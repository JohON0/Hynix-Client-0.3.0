package io.hynix.utils.johon0.render.shader.shaders;

import io.hynix.utils.johon0.render.shader.exception.IShader;

public class FaceRound implements IShader {
    @Override
    public String glsl() {
        return """
                                        #version 120
                                                                   \s
                                        uniform sampler2D texture;
                                        uniform vec2 size;
                                        uniform float radius;
                                        uniform float hurt_time;
                                        uniform float alpha;
                                        uniform float startX, endX;
                                        uniform float startY, endY;
                                                                   \s
                                        uniform float texXSize;
                                        uniform float texYSize;
                                                                   \s
                                        float signedDistanceField(vec2 p, vec2 b, float r) {
                                            return length(max(abs(p) - b, 0.0)) - r;
                                        }
                                                                   \s
                                        void main() {
                                            vec2 tex = gl_TexCoord[0].st;
                                            vec2 clippedTexCoord = vec2(
                                            mix(startX / texXSize, endX / texXSize, tex.x),
                                            mix(startY / texYSize, endY / texYSize, tex.y)
                                            );
                                            vec4 smpl = texture2D(texture, clippedTexCoord);
                                            vec2 pixel = tex * size;
                                            vec2 centre = 0.5 * size;
                                            float sa = smoothstep(0.0, 1, signedDistanceField(centre - pixel, centre - radius - 1, radius));
                                            vec4 c = mix(vec4(smpl.rgb, smpl.a), vec4(smpl.rgb, 0), sa);
                                            gl_FragColor = vec4(mix(smpl.rgb, vec3(1.0, 0.0, 0.0), hurt_time), c.a * alpha);
                                        }
                """;
    }
}
