#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec2 u_texelSize;
varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec4 texel = texture2D(u_texture, v_texCoords);
    vec2 dx = vec2(u_texelSize.x, 0.0);
    vec2 dy = vec2(0.0, u_texelSize.y);
    float neighborAlpha = max(
        max(texture2D(u_texture, v_texCoords + dx).a,
            texture2D(u_texture, v_texCoords - dx).a),
        max(texture2D(u_texture, v_texCoords + dy).a,
            texture2D(u_texture, v_texCoords - dy).a));

    // One source pixel of pale cyan, independent of the suit's tint.
    float edgeAlpha = max(0.0, neighborAlpha - texel.a) * 0.8;
    float alpha = texel.a + edgeAlpha;
    vec3 color = (texel.rgb * v_color.rgb * texel.a
        + vec3(0.65, 0.92, 1.0) * edgeAlpha) / max(alpha, 0.0001);
    gl_FragColor = vec4(color, alpha * v_color.a);
}
