precision mediump float;

uniform sampler2D sTexture;
varying vec2 texCoord;
uniform float f_alpha; // mask texture
uniform vec2 uCenter;
uniform vec2 uCenter2;

vec4 mainImage(vec2 fragCoord, vec4 WebCamPixelAt)
{
    vec4 fragColor;
    vec2 uv = fragCoord;

    //Coordintaes of the center
    float x0 = 0.5;
    float y0 = 0.5;

    //Large elipse
    float width_ellipse_1 = 0.6;
    float height_ellipse_1 = 0.7;

    //Small ellipse
    float width_ellipse_2 = 0.587;
    float height_ellipse_2 = 0.686;



    //If we are outside the large ellipse
    if (4.0*(uv.x - x0)*(uv.x - x0)/(width_ellipse_1*width_ellipse_1) + 4.0*(uv.y - y0)*(uv.y - y0)/(height_ellipse_1*height_ellipse_1) > 1.0)
    {
        vec2 newCoor = vec2((uv.x - x0) / 1.2 + x0, (uv.y - y0) / 1.2 + y0);
        fragColor = mix(texture2D(sTexture, newCoor), vec4(0.1, 0.0, 0.0, 1.0), 0.6);
    }
    else
    {
        //Check that we are inside small ellipse
        if(4.0*(uv.x - x0)*(uv.x - x0)/(width_ellipse_2*width_ellipse_2) + 4.0*(uv.y - y0)*(uv.y - y0)/(height_ellipse_2*height_ellipse_2) < 1.0)
        {
            fragColor = WebCamPixelAt;
        }
        else
        {
            // Border of mirror
            fragColor = vec4(1.0, 1.0, 1.0, 1.0);
        }

    }
    return fragColor;
}

void main()
{
    vec4 col = texture2D(sTexture, texCoord);
    gl_FragColor = mainImage(texCoord, col);
}