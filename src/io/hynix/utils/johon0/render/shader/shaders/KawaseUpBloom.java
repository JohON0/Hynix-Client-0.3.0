package io.hynix.utils.johon0.render.shader.shaders;

import io.hynix.utils.johon0.render.shader.exception.IShader;

public class KawaseUpBloom implements IShader {
    @Override
    public String glsl() {
        return """
                #version 120
                
                uniform sampler2D inTexture, textureToCheck;
                uniform vec2 halfpixel, offset, iResolution;
                uniform float saturation;
                uniform int check;
                
                void main() {
                    vec2 uv = gl_FragCoord.xy / iResolution;
                    vec4 sum = vec4(0.0);
                   \s
                    // Сохраняем значения для смещений
                    vec2 offsets[8];
                    offsets[0] = vec2(-halfpixel.x * 2.0, 0.0);
                    offsets[1] = vec2(-halfpixel.x, halfpixel.y);
                    offsets[2] = vec2(0.0, halfpixel.y * 2.0);
                    offsets[3] = vec2(halfpixel.x, halfpixel.y);
                    offsets[4] = vec2(halfpixel.x * 2.0, 0.0);
                    offsets[5] = vec2(halfpixel.x, -halfpixel.y);
                    offsets[6] = vec2(0.0, -halfpixel.y * 2.0);
                    offsets[7] = vec2(-halfpixel.x, -halfpixel.y);
                   \s
                    // Суммируем текстуры
                    for (int i = 0; i < 8; i++) {
                        vec4 sample = texture2D(inTexture, uv + offsets[i] * offset);
                        sample.rgb *= sample.a; // Применяем альфа
                        if (i == 0 || i == 4 || i == 2 || i == 6) {
                            sum += sample; // 1x вес для крайние образцы
                        } else {
                            sum += sample * 2.0; // 2x вес для соседних образцов
                        }
                    }
                
                    vec4 result = sum / 12.0; // Нормализация
                    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a), check));
                }
                """;
    }
}
