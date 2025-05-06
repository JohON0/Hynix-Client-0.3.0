package io.hynix.utils.johon0.render.shader.exception;

import io.hynix.utils.johon0.render.shader.shaders.FaceRound;
import io.hynix.utils.johon0.render.shader.shaders.*;
import lombok.Getter;

@Getter
public class Shaders {
    @Getter
    private final static Shaders Instance = new Shaders();
    private final IShader font = new FontGlsl();
    private final IShader vertex = new VertexGlsl();
    private final IShader rounded = new RoundedGlsl();
    private final IShader roundedout = new RoundedOutGlsl();
    private final IShader smooth = new SmoothGlsl();
    private final IShader white = new WhiteGlsl();
    private final IShader alpha = new AlphaGlsl();
    private final IShader gaussianbloom = new GaussianBloomGlsl();
    private final IShader kawaseUp = new KawaseUpGlsl();
    private final IShader kawaseDown = new KawaseDownGlsl();
    private final IShader outline = new OutlineGlsl();
    private final IShader contrast = new ContrastGlsl();
    private final IShader mask = new MaskGlsl();
    private final IShader MainMenuShader = new MainMenuGlsl();
    private final IShader gradient = new GradientGlsl();
    private final IShader roundedTex = new RoundedTextureGlsl();
    private final IShader outlineEsp = new OutlineESPGlsl();
    private final IShader outlineC = new OutlineCGlsl();
    private final IShader blur = new BlurGlsl();
    private final IShader blurC = new BlurCGlsl();
    private final IShader kawaseUpBloom = new KawaseUpBloom();
    private final IShader kawaseDownBloom = new KawaseDownBloom();
    private final IShader roundedFace = new RoundedFaceGlsl();
    private final IShader smoothGradient = new GradientSmoothGlsl();
    private final IShader faceRond = new FaceRound();

}
