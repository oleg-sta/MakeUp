precision mediump float;        // Set the default precision to medium. We don't need as high of a
                                // precision in the fragment shader.
// draw 2d textures
uniform sampler2D u_TextureOrig; // original screen texture
uniform sampler2D u_Texture; // mask texture
uniform sampler2D u_Texture2;
uniform sampler2D u_Texture3;

uniform vec3 color0; // color
uniform vec3 color1; // color
uniform vec3 color2; // color

varying vec2 v_TexCoordinate;
varying vec2 v_TexOrigCoordinate;
uniform vec3 f_alpha; // mask texture
uniform int useHsv;

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main()
{
    vec4 res = vec4(1.0);
    vec4 maskColor = texture2D(u_Texture, v_TexCoordinate);
    vec4 maskColor2 = texture2D(u_Texture2, v_TexCoordinate);
    vec4 maskColor3 = texture2D(u_Texture3, v_TexCoordinate);
    vec4 origColor = texture2D(u_TextureOrig, v_TexOrigCoordinate);
    if (useHsv == 1)
    {
    vec3 orig = rgb2hsv(vec3(origColor.r, origColor.g, origColor.b));
    vec3 mask = rgb2hsv(vec3(maskColor.r, maskColor.g, maskColor.b));
    orig[0] = mask[0];
    orig[1] = mask[1];
    orig = hsv2rgb(orig);
    res = mix(origColor, vec4(orig, 1.0), maskColor[3]);
    }
    else
    {
    res = mix(origColor, vec4(color0, 1.0), maskColor[3] * f_alpha[0]);
    res = mix(res, vec4(color1, 1.0), maskColor2[3] * f_alpha[1]);
    res = mix(res, vec4(color2, 1.0), maskColor3[3] * f_alpha[2]);
    }
    gl_FragColor = res;
}