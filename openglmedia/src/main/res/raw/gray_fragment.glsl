%1s

precision mediump float;

uniform %2s uTexture;
uniform vec3 uChangeColor;

varying vec2 vCoordinate;

void main(){
    vec4 nColor=texture2D(uTexture,vCoordinate);
    float c=nColor.r*uChangeColor.r+nColor.g*uChangeColor.g+nColor.b*uChangeColor.b;
    gl_FragColor=vec4(c,c,c,nColor.a);
}





