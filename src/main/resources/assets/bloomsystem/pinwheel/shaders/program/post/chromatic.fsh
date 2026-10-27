uniform sampler2D DiffuseSampler0;

in vec2 texCoord;
out vec4 fragColor;

// 原创后处理：径向色差色散 + 色相滚动 + 暗角。
// 对应需求中的「全局色差色散 shader」，由 Veil PostPipeline（bloomsystem:chromatic）驱动。
void main() {
    vec2 uv = texCoord;
    vec2 centered = uv - 0.5;
    float radius = length(centered);
    vec2 dir = radius > 0.0001 ? centered / radius : vec2(0.0);

    float strength = 0.0045 * (0.35 + radius);
    vec3 color;
    color.r = texture(DiffuseSampler0, uv + dir * strength).r;
    color.g = texture(DiffuseSampler0, uv).g;
    color.b = texture(DiffuseSampler0, uv - dir * strength).b;

    // 色相滚动：沿径向叠加轻微彩虹偏移（动态彩虹，不预烘焙）
    float hueShift = radius * 0.25;
    vec3 tint = vec3(
        0.95 + 0.05 * sin(hueShift * 6.283 + 0.0),
        0.95 + 0.05 * sin(hueShift * 6.283 + 2.094),
        0.95 + 0.05 * sin(hueShift * 6.283 + 4.188)
    );
    color = mix(color, color * tint, 0.35);

    // 暗角
    color *= clamp(1.0 - radius * radius * 0.38, 0.0, 1.0);
    fragColor = vec4(color, 1.0);
}
