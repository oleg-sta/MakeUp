precision mediump float;        // Set the default precision to medium. We don't need as high of a
                                // precision in the fragment shader.
// draw 2d textures
uniform sampler2D u_TextureOrig; // original screen texture
uniform sampler2D u_Texture; // mask texture
uniform sampler2D u_Texture1;
uniform sampler2D u_Texture2;
uniform sampler2D u_Texture3;
uniform sampler2D u_Texture4;
uniform sampler2D u_Texture5;
uniform sampler2D u_Texture6;

uniform vec3 color0; // color
uniform vec3 color1; // color
uniform vec3 color2; // color
uniform vec3 color3; // color
uniform vec3 color4; // color
uniform vec3 color5; // color
uniform vec3 color6; // color

varying vec2 v_TexCoordinate;
varying vec2 v_TexOrigCoordinate;

uniform vec3 f_alpha; // make it array
uniform vec3 f_alpha1;
uniform vec3 f_alpha2;
uniform int useHsv0[3]; // make it more
uniform int useHsv1[3];
uniform int useHsv2[3];
//uniform vec3 useHsv;
//uniform vec3 useHsv1;
//uniform vec3 useHsv2;

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

vec4 mixHsv(vec4 origColor, vec4 maskColor, vec3 colorRgb, float alpha)
{
    vec3 orig = rgb2hsv(vec3(origColor.r, origColor.g, origColor.b));
    vec3 mask = rgb2hsv(vec3(colorRgb.r, colorRgb.g, colorRgb.b));
    orig[0] = mask[0];
    orig[1] = mask[1];
    orig = hsv2rgb(orig);
    if (orig[0] < 0.0) orig[0] = 0.0;
    if (orig[0] > 1.0) orig[0] = 1.0;
    if (orig[1] < 0.0) orig[1] = 0.0;
    if (orig[1] > 1.0) orig[1] = 1.0;
    if (orig[2] < 0.0) orig[2] = 0.0;
    if (orig[2] > 1.0) orig[2] = 1.0;
    return mix(origColor, vec4(orig, 1.0), maskColor[3] * alpha);
}

vec4 toGrayscale(vec4 color)
{
  float average = (color.r + color.g + color.b) / 3.0;
  return vec4(average, average, average, 1.0);
}

vec4 colorize(vec4 grayscale, vec4 color)
{
    return (grayscale * color);
}

vec4 colorizeCommon(vec4 pic, vec4 to, float alpha)
{
    vec4 grayscale = toGrayscale(pic);
    vec4 colorizedOutput = colorize(grayscale, to);
    colorizedOutput = mix(pic, colorizedOutput, alpha);
    return colorizedOutput;
}

vec4 smartMix(vec4 inColor, int useHsvF, vec3 color, float f_alpha, vec4 maskColor)
{
    //useHsvF = 3.0;
    vec4 res = inColor;
    if (useHsvF == 1)
    {
        res = mixHsv(res, maskColor, color, f_alpha);
    } else if (useHsvF == 0) {
        res = mix(res, vec4(color, 1.0), maskColor[3] * f_alpha);
    } else if (useHsvF == 2) {
        res = colorizeCommon(res, vec4(color, 1.0), maskColor[3] * f_alpha);
    } else if (useHsvF == 3) { // use color from texture
        res = colorizeCommon(res, maskColor, maskColor[3] * f_alpha);
    }  else if (useHsvF == 4) { // use color from texture
        res = mix(res, maskColor, maskColor[3] * f_alpha);
    } else {
    // defualt mix for test purposes
        //res = mix(res, maskColor, maskColor[3] * f_alpha);
    }
//res = mix(res, vec4(1.0), maskColor[3] * f_alpha);
    return res;
}

void main()
{
    vec4 res = vec4(1.0);
    vec4 maskColor0 = texture2D(u_Texture, v_TexCoordinate);
    vec4 maskColor1 = texture2D(u_Texture1, v_TexCoordinate);
    vec4 maskColor2 = texture2D(u_Texture2, v_TexCoordinate);
    vec4 maskColor3 = texture2D(u_Texture3, v_TexCoordinate);
    vec4 maskColor4 = texture2D(u_Texture4, v_TexCoordinate);

    vec4 maskColor5 = texture2D(u_Texture5, v_TexCoordinate);
    vec4 maskColor6 = texture2D(u_Texture6, v_TexCoordinate);

    res = texture2D(u_TextureOrig, v_TexOrigCoordinate);
    res = smartMix(res, useHsv0[0], color0, f_alpha[0], maskColor0);
    res = smartMix(res, useHsv0[1], color1, f_alpha[1], maskColor1);
    res = smartMix(res, useHsv0[2], color2, f_alpha[2], maskColor2);
    res = smartMix(res, useHsv1[0], color3, f_alpha1[0], maskColor3);
    res = smartMix(res, useHsv1[1], color4, f_alpha1[1], maskColor4);

    res = smartMix(res, useHsv1[2], color5, f_alpha1[2], maskColor5);
    res = smartMix(res, useHsv2[0], color6, f_alpha2[0], maskColor6);
    gl_FragColor = res;
}