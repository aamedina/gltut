#version 410 core

layout (location = 0) in vec4 position;

uniform mat4 cameraToClipMatrix;
uniform mat4 worldToCameraMatrix;
uniform mat4 modelToWorldMatrix;

void main () {
  vec4 temp = worldToCameraMatrix * (modelToWorldMatrix * position);
  gl_Position = cameraToClipMatrix * temp;
}
