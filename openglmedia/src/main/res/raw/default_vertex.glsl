attribute vec4 aPosition;
attribute vec4 aCoordinate;
uniform mat4 uMatrix;
uniform mat4 uSTMatrix;
varying vec2 vCoordinate;
void main ()
{
  gl_Position = (uMatrix * aPosition);
  vCoordinate = (%s * aCoordinate).xy;
}