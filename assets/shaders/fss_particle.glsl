precision mediump float;        // Set the default precision to medium. We don't need as high of a
                                // precision in the fragment shader.
uniform sampler2D u_TextureOrig; // original screen texture
uniform sampler2D u_Texture; // mask texture
varying vec2 v_TexCoordinate;
varying vec2 v_TexOrigCoordinate;
uniform float f_alpha; // mask texture


void main()
{
vec4 FragColor = vec4(1.0);
FragColor.a *= 1.0;
gl_FragColor = FragColor;
}