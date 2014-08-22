#version 410 core

layout (location = 0) in vec4 position;
layout (location = 1) in vec4 color;

smooth out vec4 interpColor;

uniform mat4 cameraToClipMatrix;
uniform mat4 worldToCameraMatrix;
uniform mat4 modelToWorldMatrix;

void main () {
  vec4 temp = worldToCameraMatrix * (modelToWorldMatrix * position);
  gl_Position = cameraToClipMatrix * temp;
  interpColor = color;
}
