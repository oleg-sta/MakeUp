attribute vec2 vPosition;
attribute vec2 vTexCoord;
varying vec2 texCoord;
uniform mat4 uMVP;

// for 2d triangles
varying vec2 v_TexCoordinate;
varying vec2 v_TexOrigCoordinate;

// simple coomon 2d shader

void main() {
  texCoord = vTexCoord;
  v_TexCoordinate = vTexCoord;
  v_TexOrigCoordinate = vec2(vPosition.x / 2.0 + 0.5, vPosition.y / 2.0 + 0.5);
  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );
  gl_PointSize = 3.0; // for particles only
}