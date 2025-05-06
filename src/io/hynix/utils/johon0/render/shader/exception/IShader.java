package io.hynix.utils.johon0.render.shader.exception;

public interface IShader {

    String glsl();

    default String getName() {
        return "SHADERNONAME";
    }

}
