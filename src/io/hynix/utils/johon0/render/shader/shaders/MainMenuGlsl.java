package io.hynix.utils.johon0.render.shader.shaders;

import io.hynix.utils.johon0.render.shader.exception.IShader;

public class MainMenuGlsl implements IShader {
    @Override
    public String glsl() {
        return """
                #ifdef GL_ES
                precision mediump float;
                #endif
                
                #extension GL_OES_standard_derivatives : enable
                
                uniform float time;
                uniform float width;  // Заменяем resolution.x на width
                uniform float height; // Заменяем resolution.y на height                
                float snow(vec2 uv,float scale)
                {
                	float w=smoothstep(20.,0.,-uv.y*(scale/10.));
                	if(w<.1)return 0.;
                	uv+=time/scale;uv.y+=time*2./scale;
                //	uv.x+=sin(uv.y+time*.5)/scale;
                //	uv*=scale;
                	vec2 s=floor(uv),f=fract(uv),p;
                	float k=3.,d;
                //	p=.5+.35*sin(11.*fract(sin((s+p+scale)*mat2(7,3,6,5))*5.))-f;d=length(p);k=min(d,k);
                	p=.5-f;
                	d=length(p);
                	k=min(d,k);
                	k=smoothstep(0.,k,sin(f.x+f.y)*0.01);
                    	return k*w;
                }
                
                void main(void){
                vec2 uv = (gl_FragCoord.xy * 2. - vec2(width, height)) / min(width, height);
                vec3 finalColor=vec3(0);
                	float c=smoothstep(1.,0.1,clamp(uv.y*.1+.9,0.,.99));
                	c+=snow(uv,1.)*.3;
                	c+=snow(uv,2.)*.5;
                	c+=snow(uv,3.)*.8;
                	c+=snow(uv,4.)*.10;
                	c+=snow(uv,5.)*.12;
                	c+=snow(uv,10.)*.14;
                	c+=snow(uv,15.)*.16;
                	finalColor=(vec3(c, c, c));
                	gl_FragColor = vec4(finalColor,1);
                }
                """;

    }


}
