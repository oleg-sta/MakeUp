attribute vec3 vPosition;
attribute vec2 a_TexCoordinate;
varying vec2 v_TexCoordinate;
varying vec2 v_TexOrigCoordinate;
uniform mat4 u_MVPMatrix; // A constant representing the combined model/view/projection matrix

void main() {
  v_TexCoordinate = a_TexCoordinate;
  gl_Position = u_MVPMatrix * vec4(vPosition, 1.0);
  v_TexOrigCoordinate = gl_Position.xy;
  float z = 1.0;
  if (gl_Position.z != 0.0) {z = 1.0 / gl_Position.z;};
  v_TexOrigCoordinate.x = (v_TexOrigCoordinate.x * z + 1.0) / 2.0;
  v_TexOrigCoordinate.y = (v_TexOrigCoordinate.y * z + 1.0) / 2.0;
  gl_PointSize = 10.0;
}