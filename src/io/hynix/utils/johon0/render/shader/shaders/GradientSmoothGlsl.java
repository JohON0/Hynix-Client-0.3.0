package io.hynix.utils.johon0.render.shader.shaders;

import io.hynix.utils.johon0.render.shader.exception.IShader;

public class GradientSmoothGlsl implements IShader {
    @Override
    public String glsl() {
        return "\n" +
                "uniform vec2 location, rectSize;\n" +
                "uniform vec4 color1, color2, color3, color4;\n" +
                "uniform float radius;\n" +
                "uniform bool blur;\n" +
                "\n" +
                "float roundSDF(vec2 p, vec2 b, float r) {\n" +
                "    return length(max(abs(p) - b, 0.0)) - r;\n" +
                "}\n" +
                "\n" +
                "vec4 createGradient(vec2 coords) {\n" +
                "    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n" +
                "    return color;\n" +
                "}\n" +
                "\n" +
                "void main() {\n" +
                "    vec2 rectHalf = rectSize * .5;\n" +
                "    float smoothedAlpha = (1.0 - smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * createGradient(gl_TexCoord[0].st).a;\n" +
                "    gl_FragColor = vec4(createGradient(gl_TexCoord[0].st).rgb, smoothedAlpha);\n" +
                "}";
    }
}
