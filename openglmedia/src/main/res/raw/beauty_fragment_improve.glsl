%1s

precision highp float;

uniform %2s uTexture;
uniform int uIternum;
uniform float uAaCoef; //参数
uniform float uMixCoef; //混合系数
uniform vec2 singleStepOffset;

varying highp vec2 vCoordinate;

const highp vec3 W = vec3(0.299,0.587,0.114);
const mat3 saturateMatrix = mat3(
		1.1102,-0.0598,-0.061,
		-0.0774,1.0826,-0.1186,
		-0.0228,-0.0228,1.1772);

float hardlight(float color)
{
	if(color <= 0.5)
	{
		color = color * color * 2.0;
	}
	else
	{
		color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);
	}
	return color;
}

void main(){
	vec2 blurCoordinates[12];

	blurCoordinates[0] = vCoordinate.xy + singleStepOffset * vec2(5.0, -8.0);
	blurCoordinates[1] = vCoordinate.xy + singleStepOffset * vec2(5.0, 8.0);
	blurCoordinates[2] = vCoordinate.xy + singleStepOffset * vec2(-5.0, 8.0);
	blurCoordinates[3] = vCoordinate.xy + singleStepOffset * vec2(-5.0, -8.0);

	blurCoordinates[4] = vCoordinate.xy + singleStepOffset * vec2(8.0, -5.0);
	blurCoordinates[5] = vCoordinate.xy + singleStepOffset * vec2(8.0, 5.0);
	blurCoordinates[6] = vCoordinate.xy + singleStepOffset * vec2(-8.0, 5.0);
	blurCoordinates[7] = vCoordinate.xy + singleStepOffset * vec2(-8.0, -5.0);

	blurCoordinates[8] = vCoordinate.xy + singleStepOffset * vec2(-4.0, -4.0);
	blurCoordinates[9] = vCoordinate.xy + singleStepOffset * vec2(-4.0, 4.0);
	blurCoordinates[10] = vCoordinate.xy + singleStepOffset * vec2(4.0, -4.0);
	blurCoordinates[11] = vCoordinate.xy + singleStepOffset * vec2(4.0, 4.0);

	float sampleColor = texture2D(uTexture, vCoordinate).g * 22.0;

	sampleColor += texture2D(uTexture, blurCoordinates[0]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[1]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[2]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[3]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[4]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[5]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[6]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[7]).g;

	sampleColor += texture2D(uTexture, blurCoordinates[8]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[9]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[10]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[11]).g * 2.0;

	sampleColor = sampleColor / 38.0;

	vec3 centralColor = texture2D(uTexture, vCoordinate).rgb;
	
	float highpass = centralColor.g - sampleColor + 0.5;
	
	for(int i = 0; i < uIternum;i++)
	{
		highpass = hardlight(highpass);
	}

    float aa = 1.0 + pow( centralColor.g, 0.3 )*uAaCoef;
    vec3 smoothColor = centralColor*aa - vec3( highpass )*(aa - 1.0);

    smoothColor = clamp( smoothColor, vec3( 0.0 ), vec3( 1.0 ) );
    smoothColor = mix( centralColor, smoothColor, pow( centralColor.g, 0.33 ) );
    smoothColor = mix( centralColor, smoothColor, pow( centralColor.g, uMixCoef ) );

    gl_FragColor = vec4( pow( smoothColor, vec3( 0.96 ) ), 1.0 );

    vec3 satcolor = gl_FragColor.rgb * saturateMatrix;
    gl_FragColor.rgb = mix( gl_FragColor.rgb, satcolor, 0.23 );
}