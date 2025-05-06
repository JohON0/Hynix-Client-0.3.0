package io.hynix.utils.johon0.render.shader.shaders;

import io.hynix.utils.johon0.render.shader.exception.IShader;

public class RoundedTextureGlsl implements IShader {
    @Override
    public String glsl() {
        return """
           #version 120
                // объявление переменных
                uniform vec2 size; // размер прямоугольника
                uniform vec4 round; // коэффициенты скругления углов
                uniform vec2 smoothness; // плавность перехода от цвета к прозрачности
                uniform float value; // значение, используемое для расчета расстояния до границы
                uniform sampler2D textureIn;
                uniform float alpha;

                // функция для расчета расстояния до границы
                float test(vec2 vec_1, vec2 vec_2, vec4 vec_4) {
                    vec_4.xy = (vec_1.x > 0.0) ? vec_4.xy : vec_4.zw;
                    vec_4.x = (vec_1.y > 0.0) ? vec_4.x : vec_4.y;
                    vec2 coords = abs(vec_1) - vec_2 + vec_4.x;
                    return min(max(coords.x, coords.y), 0.0) + length(max(coords, vec2(0.0f))) - vec_4.x;
                }

                void main() {
                    vec4 color = texture2D(textureIn, gl_TexCoord[0].st);
                    vec2 st = gl_TexCoord[0].st * size; // координаты текущего пикселя
                    vec2 halfSize = 0.5 * size; // половина размера прямоугольника
                    float sa = 1.0 - smoothstep(smoothness.x, smoothness.y, test(halfSize - st, halfSize - value, round));
                    // рассчитываем прозрачность в зависимости от расстояния до границы
                    gl_FragColor = mix(vec4(color.rgb, 0.0), vec4(color.rgb, alpha), sa); // устанавливаем цвет прямоугольника с прозрачностью sa
                }""";
    }
}
