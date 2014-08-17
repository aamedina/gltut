#version 410

layout (location = 0) in vec4 position;

uniform float loopDuration;
uniform float time;

void main () {
  float timeScale = 3.14159 * 2.0 / loopDuration;
  float currTime = mod(time, loopDuration);
  vec4 totalOffset = vec4(cos(currTime * timeScale),
                          sin(currTime * timeScale), 0.0, 0.0);
  gl_Position = position + totalOffset;
}
