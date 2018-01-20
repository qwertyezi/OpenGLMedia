%1s

precision mediump float;

uniform %2s uTexture;

varying vec2 vCoordinate;

void main(){
    gl_FragColor=texture2D(uTexture,vCoordinate);
}