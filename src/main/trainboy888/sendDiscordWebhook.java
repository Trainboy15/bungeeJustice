package trainboy888;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

private void sendDiscordWebhook(String punishment, String target, String actor, String reason, String duration) {
    if (!config.getBoolean("discord-webhook.enabled", false)) return;
    
    String webhookUrl = config.getString("discord-webhook.url", "");
    if (webhookUrl.isEmpty() || webhookUrl.contains("YOUR_ID")) return;

    ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
        try {
            Map<String, String> placeholders = Map.of(
                "{punishment}", punishment,
                "{target}", target,
                "{actor}", actor,
                "{reason}", reason,
                "{duration}", duration
            );

            String username = applyPlaceholders(config.getString("discord-webhook.username", "Punishments"), placeholders);
            String avatarUrl = config.getString("discord-webhook.avatar-url", "");
            String title = applyPlaceholders(config.getString("discord-webhook.embed.title", "Punishment"), placeholders);
            String description = applyPlaceholders(config.getString("discord-webhook.embed.description", ""), placeholders);
            String footer = applyPlaceholders(config.getString("discord-webhook.embed.footer", ""), placeholders);
            int color = config.getInt("discord-webhook.embed.color", 15158332);

            // Build JSON payload safely
            String json = String.format(
                "{\"username\":\"%s\",\"avatar_url\":\"%s\",\"embeds\":[{\"title\":\"%s\",\"description\":\"%s\",\"color\":%d,\"footer\":{\"text\":\"%s\"}}]}",
                escapeJson(username),
                escapeJson(avatarUrl),
                escapeJson(title),
                escapeJson(description),
                color,
                escapeJson(footer)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord punishment webhook: " + e.getMessage());
        }
    });
}

private String applyPlaceholders(String text, Map<String, String> placeholders) {
    if (text == null) return "";
    // Strip Minecraft color codes (&a, §c, etc.) before sending to Discord
    String result = text.replaceAll("(?i)[&§][0-9A-FK-ORX]", "");
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
        result = result.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
    }
    return result;
}

private String escapeJson(String text) {
    if (text == null) return "";
    return text.replace("\\", "\\\\")
               .replace("\"", "\\\"")
               .replace("\n", "\\n")
               .replace("\r", "");
}
