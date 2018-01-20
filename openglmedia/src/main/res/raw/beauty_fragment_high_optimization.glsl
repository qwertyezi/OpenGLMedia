%1s

precision mediump float;
uniform lowp %2s uTexture;
uniform lowp vec2 singleStepOffset;
uniform lowp vec4 params;
varying lowp vec2 vCoordinate;

void main(){
            lowp vec3 satcolor_1;
              lowp vec3 smoothColor_2;
              lowp float highpass_3;
              lowp float sampleColor_4;
              lowp vec4 tmpvar_5;
              tmpvar_5 = texture2D (uTexture, vCoordinate);
              sampleColor_4 = ((tmpvar_5.y * 22.0) + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(0.0, -10.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(0.0, 10.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(-10.0, 0.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(10.0, 0.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(5.0, -8.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(5.0, 8.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(-5.0, 8.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(-5.0, -8.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(8.0, -5.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(8.0, 5.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(-8.0, 5.0)))).y);
              sampleColor_4 = (sampleColor_4 + texture2D (uTexture, (vCoordinate + (singleStepOffset * vec2(-8.0, -5.0)))).y);
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(0.0, -6.0))
              )).y * 2.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(0.0, 6.0))
              )).y * 2.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(6.0, 0.0))
              )).y * 2.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(-6.0, 0.0))
              )).y * 2.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(-4.0, -4.0))
              )).y * 2.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(-4.0, 4.0))
              )).y * 2.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(4.0, -4.0))
              )).y * 2.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(4.0, 4.0))
              )).y * 2.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(-2.0, -2.0))
              )).y * 3.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(-2.0, 2.0))
              )).y * 3.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(2.0, -2.0))
              )).y * 3.0));
              sampleColor_4 = (sampleColor_4 + (texture2D (uTexture, (vCoordinate +
                (singleStepOffset * vec2(2.0, 2.0))
              )).y * 3.0));
              sampleColor_4 = (sampleColor_4 / 62.0);
              highpass_3 = ((tmpvar_5.y - sampleColor_4) + 0.5);
              lowp float color_6;
              color_6 = highpass_3;
              if ((highpass_3 <= 0.5)) {
                color_6 = ((highpass_3 * highpass_3) * 2.0);
              } else {
                color_6 = (1.0 - ((
                  (1.0 - color_6)
                 *
                  (1.0 - color_6)
                ) * 2.0));
              };
              highpass_3 = color_6;
              lowp float color_7;
              color_7 = color_6;
              if ((color_6 <= 0.5)) {
                color_7 = ((color_6 * color_6) * 2.0);
              } else {
                color_7 = (1.0 - ((
                  (1.0 - color_7)
                 *
                  (1.0 - color_7)
                ) * 2.0));
              };
              highpass_3 = color_7;
              lowp float color_8;
              color_8 = color_7;
              if ((color_7 <= 0.5)) {
                color_8 = ((color_7 * color_7) * 2.0);
              } else {
                color_8 = (1.0 - ((
                  (1.0 - color_8)
                 *
                  (1.0 - color_8)
                ) * 2.0));
              };
              highpass_3 = color_8;
              lowp float color_9;
              color_9 = color_8;
              if ((color_8 <= 0.5)) {
                color_9 = ((color_8 * color_8) * 2.0);
              } else {
                color_9 = (1.0 - ((
                  (1.0 - color_9)
                 *
                  (1.0 - color_9)
                ) * 2.0));
              };
              highpass_3 = color_9;
              lowp float color_10;
              color_10 = color_9;
              if ((color_9 <= 0.5)) {
                color_10 = ((color_9 * color_9) * 2.0);
              } else {
                color_10 = (1.0 - ((
                  (1.0 - color_10)
                 *
                  (1.0 - color_10)
                ) * 2.0));
              };
              highpass_3 = color_10;
              lowp float tmpvar_11;
              tmpvar_11 = pow (dot (tmpvar_5.xyz, vec3(0.299, 0.587, 0.114)), params.x);
              lowp vec3 tmpvar_12;
              tmpvar_12 = (tmpvar_5.xyz + ((
                (tmpvar_5.xyz - vec3(color_10))
               * tmpvar_11) * 0.1));
              smoothColor_2.x = clamp (pow (tmpvar_12.x, params.y), 0.0, 1.0);
              smoothColor_2.y = clamp (pow (tmpvar_12.y, params.y), 0.0, 1.0);
              smoothColor_2.z = clamp (pow (tmpvar_12.z, params.y), 0.0, 1.0);
              lowp vec3 tmpvar_13;
              tmpvar_13 = max (smoothColor_2, tmpvar_5.xyz);
              lowp vec3 tmpvar_14;
              tmpvar_14 = (((
                (2.0 * tmpvar_5.xyz)
               * smoothColor_2) + (tmpvar_5.xyz * tmpvar_5.xyz)) - ((2.0 * tmpvar_5.xyz) * (tmpvar_5.xyz * smoothColor_2)));
              lowp vec4 tmpvar_15;
              tmpvar_15.w = 1.0;
              tmpvar_15.xyz = mix (tmpvar_5.xyz, (vec3(1.0, 1.0, 1.0) - (
                (vec3(1.0, 1.0, 1.0) - smoothColor_2)
               *
                (vec3(1.0, 1.0, 1.0) - tmpvar_5.xyz)
              )), tmpvar_11);
              gl_FragColor = tmpvar_15;
              gl_FragColor.xyz = mix (gl_FragColor.xyz, tmpvar_13, tmpvar_11);
              gl_FragColor.xyz = mix (gl_FragColor.xyz, tmpvar_14, params.z);
              mediump vec3 tmpvar_16;
              tmpvar_16 = (gl_FragColor.xyz * mat3(1.1102, -0.0598, -0.061, -0.0774, 1.0826, -0.1186, -0.0228, -0.0228, 1.1772));
              satcolor_1 = tmpvar_16;
              gl_FragColor.xyz = mix (gl_FragColor.xyz, satcolor_1, params.w);
}