#version 410 core

smooth in vec4 interpColor;

out vec4 outputColor;

void main () {
  outputColor = interpColor;
}
