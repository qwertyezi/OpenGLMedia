%1s

precision mediump float;

uniform %2s uTexture;
uniform lowp float uBrightness;

varying vec2 vCoordinate;

void main(){
    lowp vec4 textureColor = texture2D(uTexture,vCoordinate);
    gl_FragColor = vec4((textureColor.rgb + vec3(uBrightness)), textureColor.w);
}