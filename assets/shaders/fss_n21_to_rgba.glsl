precision mediump float;
uniform sampler2D sTexture; // y - texture
uniform sampler2D sTexture2; //uv texture
varying vec2 texCoord;
uniform int u_facing;
uniform float cameraWidth;
uniform float cameraHeight;

const mat3 yuv2rgb = mat3(
                        1, 0, 1.2802,
                        1, -0.214821, -0.380589,
                        1, 2.127982, 0
                        );
// shader from convert NV21 to RGBA

void main() {
  vec2 coord = vec2(texCoord.y, texCoord.x);
  if (u_facing == 0) coord.x = 1.0 - coord.x;
  coord.y = 1.0 - coord.y;

  float hh = cameraHeight / 2.0;
  vec2 realCoord = vec2(floor(coord.x * cameraWidth + 0.5), floor(coord.y * hh + 0.5));
  vec2 uCoord = vec2(floor(realCoord.x / 2.0) * 2.0 / cameraWidth, floor(realCoord.y / 2.0) * 2.0 / hh);
  vec2 vCoord = vec2((floor(realCoord.x / 2.0) * 2.0 + 1.0) / cameraWidth, floor(realCoord.y / 2.0) * 2.0 / hh);
  uCoord = vec2(realCoord.x / cameraWidth, realCoord.y  / hh);
  vCoord = vec2(realCoord.x / cameraWidth, realCoord.y  / hh);
  uCoord = coord;
  vCoord = coord;
  float y = texture2D(sTexture, coord).r;
  float u = texture2D(sTexture2, uCoord).a;
  float v = texture2D(sTexture2, vCoord).r;
  vec4 color;
  // another way sligthly lighter
  // TODO find correct way of transfromation
  color.r = (1.164 * (y - 0.0625)) + (1.596 * (v - 0.5));
  color.g = (1.164 * (y - 0.0625)) - (0.391 * (u - 0.5)) - (0.813 * (v - 0.5));
  color.b = (1.164 * (y - 0.0625)) + (2.018 * (u - 0.5));
  color.a = 1.0;
  vec3 yuv = vec3(
                  1.1643 * y - 0.0627,
                  u - 0.5,
                  v - 0.5
                  );
      vec3 rgb = yuv * yuv2rgb;
      color = vec4(rgb, 1.0);
  gl_FragColor = color;
}