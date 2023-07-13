#version 330
in vec3 pos;
out vec3 color;
in vec3 vertColor;
void main() {
    // vertex shader is a program that will run for each vertex (setting the position)

    gl_Position = vec4(pos.x, pos.y, pos.z, 1.0);
    color = vertColor;
}
