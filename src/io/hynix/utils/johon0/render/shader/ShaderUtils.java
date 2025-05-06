package io.hynix.utils.johon0.render.shader;

import io.hynix.utils.johon0.render.shader.exception.IShader;
import io.hynix.utils.johon0.render.shader.exception.Shaders;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL20;

import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.render.shader.exception.UndefinedShader;
import net.minecraft.client.MainWindow;
import net.minecraft.client.shader.Framebuffer;

import java.io.*;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.ARBShaderObjects.*;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtils implements IMinecraft {
    private final int programID;

    public static ShaderUtils textShader = new ShaderUtils("textShader");
    public static ShaderUtils rounded = new ShaderUtils("rounded");
    public static ShaderUtils roundedFace = new ShaderUtils("roundedFace");

    public static ShaderUtils roundedout = new ShaderUtils("roundedout");
    public static ShaderUtils smooth = new ShaderUtils("smooth");
    public static ShaderUtils white = new ShaderUtils("white");
    public static ShaderUtils alpha = new ShaderUtils("alpha");
    public static ShaderUtils kawaseUp = new ShaderUtils("kawaseUp");
    public static ShaderUtils kawaseDown = new ShaderUtils("kawaseDown");
    public static ShaderUtils outline = new ShaderUtils("outline");
    public static ShaderUtils contrast = new ShaderUtils("contrast");
    public static ShaderUtils mask = new ShaderUtils("mask");
    public static ShaderUtils MainMenuShader = new ShaderUtils("MainMenuShader");
    public static ShaderUtils gradient = new ShaderUtils("gradient");
    public static ShaderUtils outlineEsp = new ShaderUtils("outlineEsp");
    public static ShaderUtils outlineC = new ShaderUtils("outlineC");
    public static ShaderUtils blur = new ShaderUtils("blur");
    public static ShaderUtils blurC = new ShaderUtils("blurC");
    public static ShaderUtils kawaseUpBloom = new ShaderUtils("kawaseUpBloom");
    public static ShaderUtils kawaseDownBloom = new ShaderUtils("kawaseDownBloom");
    public static ShaderUtils smoothGradient = new ShaderUtils("smoothGradient");

    public ShaderUtils(String fragmentShaderLoc) {
        programID = ARBShaderObjects.glCreateProgramObjectARB();

        try {
            // Загрузка фрагментного шейдера
            int fragmentShaderID = switch (fragmentShaderLoc) {
                case "textShader" -> createShader(Shaders.getInstance().getFont(), GL_FRAGMENT_SHADER);
                case "smooth" -> createShader(Shaders.getInstance().getSmooth(), GL_FRAGMENT_SHADER);
                case "white" -> createShader(Shaders.getInstance().getWhite(), GL_FRAGMENT_SHADER);
                case "rounded" -> createShader(Shaders.getInstance().getRounded(), GL_FRAGMENT_SHADER);
                case "roundedout" -> createShader(Shaders.getInstance().getRoundedout(), GL_FRAGMENT_SHADER);
                case "bloom" -> createShader(Shaders.getInstance().getGaussianbloom(), GL_FRAGMENT_SHADER);
                case "kawaseUp" -> createShader(Shaders.getInstance().getKawaseUp(), GL_FRAGMENT_SHADER);
                case "kawaseDown" -> createShader(Shaders.getInstance().getKawaseDown(), GL_FRAGMENT_SHADER);
                case "alpha" -> createShader(Shaders.getInstance().getAlpha(), GL_FRAGMENT_SHADER);
                case "outline" -> createShader(Shaders.getInstance().getOutline(), GL_FRAGMENT_SHADER);
                case "contrast" -> createShader(Shaders.getInstance().getContrast(), GL_FRAGMENT_SHADER);
                case "mask" -> createShader(Shaders.getInstance().getMask(), GL_FRAGMENT_SHADER);
                case "MainMenuShader" -> createShader(Shaders.getInstance().getMainMenuShader(), GL_FRAGMENT_SHADER);
                case "gradient" -> createShader(Shaders.getInstance().getGradient(), GL_FRAGMENT_SHADER);
                case "roundedTex" -> createShader(Shaders.getInstance().getRoundedTex(), GL_FRAGMENT_SHADER);
                case "outlineEsp" -> createShader(Shaders.getInstance().getOutlineEsp(), GL_FRAGMENT_SHADER);
                case "outlineC" -> createShader(Shaders.getInstance().getOutlineC(), GL_FRAGMENT_SHADER);
                case "blur" -> createShader(Shaders.getInstance().getBlur(), GL_FRAGMENT_SHADER);
                case "blurC" -> createShader(Shaders.getInstance().getBlurC(), GL_FRAGMENT_SHADER);
                case "kawaseDownBloom" -> createShader(Shaders.getInstance().getKawaseDownBloom(), GL_FRAGMENT_SHADER);
                case "kawaseUpBloom" -> createShader(Shaders.getInstance().getKawaseUpBloom(), GL_FRAGMENT_SHADER);
                case "roundedFace" -> createShader(Shaders.getInstance().getRoundedFace(), GL_FRAGMENT_SHADER);
                case "faceround" -> createShader(Shaders.getInstance().getFaceRond(), GL_FRAGMENT_SHADER);

                case "smoothGradient" -> createShader(Shaders.getInstance().getSmoothGradient(), GL_FRAGMENT_SHADER);
                default ->
                    throw new UndefinedShader(fragmentShaderLoc);
            };
            ARBShaderObjects.glAttachObjectARB(programID, fragmentShaderID);

            // Загрузка вершинного шейдера
            ARBShaderObjects.glAttachObjectARB(programID,
                    createShader(Shaders.getInstance().getVertex(), GL_VERTEX_SHADER));

            // Связывание шейдеров
            ARBShaderObjects.glLinkProgramARB(programID);
        } catch (UndefinedShader exception) {
            exception.fillInStackTrace();
            System.out.println("Ошибка при загрузке: " + fragmentShaderLoc);
        }
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.getMainWindow().getWidth()
                || framebuffer.framebufferHeight != mc.getMainWindow().getHeight();
    }

    public int getUniform(String name) {
        return ARBShaderObjects.glGetUniformLocationARB(programID, name);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight(), depth, false);
        }
        return framebuffer;
    }

    public void drawQuads(float x, float y, float width, float height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        glTexCoord2f(0, 1);
        glVertex2f(x, y + height);
        glTexCoord2f(1, 1);
        glVertex2f(x + width, y + height);
        glTexCoord2f(1, 0);
        glVertex2f(x + width, y);
        glEnd();
    }

    public static void drawQuads() {
        MainWindow sr = mc.getMainWindow();
        float width = (float) sr.getScaledWidth();
        float height = (float) sr.getScaledHeight();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    public Framebuffer setupBuffer(Framebuffer frameBuffer) {
        if (frameBuffer.framebufferWidth != mc.getMainWindow().getWidth()
                || frameBuffer.framebufferHeight != mc.getMainWindow().getHeight())
            frameBuffer.resize(Math.max(1, mc.getMainWindow().getWidth()), Math.max(1, mc.getMainWindow().getHeight()),
                    false);
        else
            frameBuffer.framebufferClear(false);
        frameBuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);

        return frameBuffer;
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double PI = 3.141592653;
        double output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    /**
     * Подключение шейдера к контексту OpenGL
     */
    public void attach() {
        ARBShaderObjects.glUseProgramObjectARB(programID);
    }

    /**
     * Отключение шейдера от контекста OpenGL
     */
    public void detach() {
        glUseProgram(0);
    }

    /**
     * Установка значения uniform переменной
     *
     * @param name
     * @param args
     */
    public void setUniform(String name, float... args) {
        int loc = ARBShaderObjects.glGetUniformLocationARB(programID, name);
        switch (args.length) {
            case 1 -> ARBShaderObjects.glUniform1fARB(loc, args[0]);
            case 2 -> ARBShaderObjects.glUniform2fARB(loc, args[0], args[1]);
            case 3 -> ARBShaderObjects.glUniform3fARB(loc, args[0], args[1], args[2]);
            case 4 -> ARBShaderObjects.glUniform4fARB(loc, args[0], args[1], args[2], args[3]);
            default ->
                throw new IllegalArgumentException("Недопустимое количество аргументов для uniform '" + name + "'");
        }
    }

    public void setUniform(String name, int... args) {
        int loc = ARBShaderObjects.glGetUniformLocationARB(programID, name);
        switch (args.length) {
            case 1 -> glUniform1iARB(loc, args[0]);
            case 2 -> glUniform2iARB(loc, args[0], args[1]);
            case 3 -> glUniform3iARB(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4iARB(loc, args[0], args[1], args[2], args[3]);
            default ->
                throw new IllegalArgumentException("Недопустимое количество аргументов для uniform '" + name + "'");
        }
    }

    public void setUniformf(String var1, float... var2) {
        int var3 = ARBShaderObjects.glGetUniformLocationARB(this.programID, var1);
        switch (var2.length) {
            case 1 -> ARBShaderObjects.glUniform1fARB(var3, var2[0]);
            case 2 -> ARBShaderObjects.glUniform2fARB(var3, var2[0], var2[1]);
            case 3 -> ARBShaderObjects.glUniform3fARB(var3, var2[0], var2[1], var2[2]);
            case 4 -> ARBShaderObjects.glUniform4fARB(var3, var2[0], var2[1], var2[2], var2[3]);
        }
    }

    public void setUniformf1(String name, float... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1f(loc, args[0]);
            case 2 -> glUniform2f(loc, args[0], args[1]);
            case 3 -> glUniform3f(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4f(loc, args[0], args[1], args[2], args[3]);
        }
    }

    public void setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1i(loc, args[0]);
            case 2 -> glUniform2i(loc, args[0], args[1]);
            case 3 -> glUniform3i(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4i(loc, args[0], args[1], args[2], args[3]);
        }
    }

    public void setUniformf(String var1, double... var2) {
        int var3 = ARBShaderObjects.glGetUniformLocationARB(this.programID, var1);
        switch (var2.length) {
            case 1 -> ARBShaderObjects.glUniform1fARB(var3, (float) var2[0]);
            case 2 -> ARBShaderObjects.glUniform2fARB(var3, (float) var2[0], (float) var2[1]);
            case 3 -> ARBShaderObjects.glUniform3fARB(var3, (float) var2[0], (float) var2[1], (float) var2[2]);
            case 4 -> ARBShaderObjects.glUniform4fARB(var3, (float) var2[0], (float) var2[1], (float) var2[2],
                    (float) var2[3]);
        }
    }

    private int createShader(IShader glsl, int shaderType) {
        int shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
        ARBShaderObjects.glShaderSourceARB(shader, readInputStream(new ByteArrayInputStream(glsl.glsl().getBytes())));
        ARBShaderObjects.glCompileShaderARB(shader);
        if (GL20.glGetShaderi(shader, 35713) == 0) {
            System.out.println(GL20.glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }
        return shader;
    }

    public String readInputStream(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream)).lines()
                .map(line -> line + '\n')
                .collect(Collectors.joining());
    }

}