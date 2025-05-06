package io.hynix.utils.johon0.render.shader.shaders;

import io.hynix.utils.johon0.render.shader.exception.IShader;

public class RoundedFaceGlsl implements IShader {
    @Override
    public String glsl() {
        return """
            #version 120
            
            uniform vec2 location, size;
            uniform sampler2D texture;
            uniform float radius, alpha;
            uniform float u, v, w, h;
            
            float calcLength(vec2 p, vec2 b, float r) {
                return length(max(abs(p) - b, 0)) - r;
            }
            
            void main() {
                vec2 halfSize = size * 0.5;
                vec2 st = gl_TexCoord[0].st;
                st.x = u + st.x * w;
                st.y = v + st.y * h;
                float distance = calcLength(halfSize - (gl_TexCoord[0].st * size), halfSize - radius - 1, radius);
                float smoothedAlpha = (1 - smoothstep(0, 2, distance)) * alpha;
                vec4 color = texture2D(texture, st);
                gl_FragColor = vec4(color.rgb, smoothedAlpha);
            }
            """;
    }
}
