package app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		System.out.print("Introduce el nombre de usuario de GitHub: ");
		String username = sc.nextLine();

		String endponit = String.format("https://api.github.com/users/%s/events?per_page=20", username);

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> errorMessage = new HashMap<String, Object>();

		HttpClient client = HttpClient.newHttpClient();

		try {
			HttpRequest request = HttpRequest.newBuilder().uri(new URI(endponit))
					.header("Accept", "application/vnd.github+json").header("X-GitHub-Api-Version", "2022-11-28")
					.timeout(Duration.ofSeconds(10)).GET().build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.body().equals("[]")) {
				errorMessage.put("Message", "La respuesta esta vacía");
				errorMessage.put("Endpoint", endponit);
				System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorMessage));
			}

			if (response.statusCode() == 200) {
				JsonNode jsonResponse = mapper.readTree(response.body());

				salidaMapper(jsonResponse);

			} else {
				errorMessage.put("ErrorCode", response.statusCode());
				errorMessage.put("Message", "Ha ocurrido un error");
				System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorMessage));
			}

		} catch (URISyntaxException uriSyntaxException) {
			uriSyntaxException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
			interruptedException.printStackTrace();
		}

	}

	private static void salidaMapper(JsonNode jsonNode) {

		for (JsonNode jn : jsonNode) {
			String type = jn.get("type").asText();
			String salida;

			switch (type) {
			case "PushEvent": {
				int commit = jn.get("payload").get("commits").size();
				salida = "Pushed " + commit + " commits " + "to " + jn.get("repo").get("name").asText();
				break;
			}
			case "CreateEvent": {
				salida = "Opened a new " + jn.get("payload").get("ref_type") + " in "
						+ jn.get("repo").get("name").asText();
				break;
			}
			case "WatchEvent": {
				salida = "Starred " + jn.get("repo").get("name").asText();
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + type);
			}

			System.out.println(salida);

		}
	}

}
