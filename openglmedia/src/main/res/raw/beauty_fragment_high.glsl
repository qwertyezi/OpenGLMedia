%1s

precision mediump float;

uniform %2s uTexture;

uniform lowp vec2 singleStepOffset;
uniform lowp vec4 params;

varying lowp vec2 vCoordinate;

const lowp vec3 W = vec3(0.299,0.587,0.114);
const lowp mat3 saturateMatrix = mat3(
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
	lowp vec2 blurCoordinates[24];
	
	blurCoordinates[0] = vCoordinate.xy + singleStepOffset * vec2(0.0, -10.0);
	blurCoordinates[1] = vCoordinate.xy + singleStepOffset * vec2(0.0, 10.0);
	blurCoordinates[2] = vCoordinate.xy + singleStepOffset * vec2(-10.0, 0.0);
	blurCoordinates[3] = vCoordinate.xy + singleStepOffset * vec2(10.0, 0.0);
	
	blurCoordinates[4] = vCoordinate.xy + singleStepOffset * vec2(5.0, -8.0);
	blurCoordinates[5] = vCoordinate.xy + singleStepOffset * vec2(5.0, 8.0);
	blurCoordinates[6] = vCoordinate.xy + singleStepOffset * vec2(-5.0, 8.0);
	blurCoordinates[7] = vCoordinate.xy + singleStepOffset * vec2(-5.0, -8.0);
	
	blurCoordinates[8] = vCoordinate.xy + singleStepOffset * vec2(8.0, -5.0);
	blurCoordinates[9] = vCoordinate.xy + singleStepOffset * vec2(8.0, 5.0);
	blurCoordinates[10] = vCoordinate.xy + singleStepOffset * vec2(-8.0, 5.0);	
	blurCoordinates[11] = vCoordinate.xy + singleStepOffset * vec2(-8.0, -5.0);
	
	blurCoordinates[12] = vCoordinate.xy + singleStepOffset * vec2(0.0, -6.0);
	blurCoordinates[13] = vCoordinate.xy + singleStepOffset * vec2(0.0, 6.0);
	blurCoordinates[14] = vCoordinate.xy + singleStepOffset * vec2(6.0, 0.0);
	blurCoordinates[15] = vCoordinate.xy + singleStepOffset * vec2(-6.0, 0.0);	
	
	blurCoordinates[16] = vCoordinate.xy + singleStepOffset * vec2(-4.0, -4.0);
	blurCoordinates[17] = vCoordinate.xy + singleStepOffset * vec2(-4.0, 4.0);
	blurCoordinates[18] = vCoordinate.xy + singleStepOffset * vec2(4.0, -4.0);
	blurCoordinates[19] = vCoordinate.xy + singleStepOffset * vec2(4.0, 4.0);
	
	blurCoordinates[20] = vCoordinate.xy + singleStepOffset * vec2(-2.0, -2.0);
	blurCoordinates[21] = vCoordinate.xy + singleStepOffset * vec2(-2.0, 2.0);
	blurCoordinates[22] = vCoordinate.xy + singleStepOffset * vec2(2.0, -2.0);
	blurCoordinates[23] = vCoordinate.xy + singleStepOffset * vec2(2.0, 2.0);
	
	
	float sampleColor = texture2D(uTexture, vCoordinate).g * 22.0;
	sampleColor += texture2D(uTexture, blurCoordinates[0]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[1]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[2]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[3]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[4]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[5]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[6]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[7]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[8]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[9]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[10]).g;
	sampleColor += texture2D(uTexture, blurCoordinates[11]).g;
	
	sampleColor += texture2D(uTexture, blurCoordinates[12]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[13]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[14]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[15]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[16]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[17]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[18]).g * 2.0;
	sampleColor += texture2D(uTexture, blurCoordinates[19]).g * 2.0;	
	
	sampleColor += texture2D(uTexture, blurCoordinates[20]).g * 3.0;
	sampleColor += texture2D(uTexture, blurCoordinates[21]).g * 3.0;
	sampleColor += texture2D(uTexture, blurCoordinates[22]).g * 3.0;
	sampleColor += texture2D(uTexture, blurCoordinates[23]).g * 3.0;	
		
	sampleColor = sampleColor / 62.0;
	
	lowp vec3 centralColor = texture2D(uTexture, vCoordinate).rgb;

	float highpass = centralColor.g - sampleColor + 0.5;
	
	for(int i = 0; i < 5;i++)
	{
		highpass = hardlight(highpass);
	}
	float lumance = dot(centralColor, W);
	
	float alpha = pow(lumance, params.r);

	lowp vec3 smoothColor = centralColor + (centralColor-vec3(highpass))*alpha*0.1;
	
	smoothColor.r = clamp(pow(smoothColor.r, params.g),0.0,1.0);
	smoothColor.g = clamp(pow(smoothColor.g, params.g),0.0,1.0);
	smoothColor.b = clamp(pow(smoothColor.b, params.g),0.0,1.0);
	
	lowp vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);
	lowp vec3 bianliang = max(smoothColor, centralColor);
    lowp vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;
	
	gl_FragColor = vec4(mix(centralColor, lvse, alpha), 1.0);
	gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, alpha);
	gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, params.b);
	
	lowp vec3 satcolor = gl_FragColor.rgb * saturateMatrix;
	gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, params.a);
}