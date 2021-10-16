#version 330 core

in vec2 textureCoord;

uniform vec4 color;
uniform sampler2D sampler;

void main() {
    gl_FragColor = texture2D(sampler, textureCoord) * color;
}