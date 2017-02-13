precision mediump float;

uniform sampler2D sTexture;
varying vec2 texCoord;
uniform float f_alpha; // mask texture
uniform vec2 uCenter;
uniform vec2 uCenter2;

void main()
{
    gl_FragColor = texture2D(sTexture, texCoord);
}