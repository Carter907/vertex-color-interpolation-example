#version 330
out vec4 fragColor;
in vec3 color;
void main() {
    fragColor = vec4(color.r,color.g,color.b,1f);
}
