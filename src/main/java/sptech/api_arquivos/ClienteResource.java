package sptech.api_arquivos;

import java.util.Base64;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.s3.S3Client;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteResource {

  private final ClienteRepository repository;
  private final Region region = Region.US_EAST_1;
  private final S3Client s3 = S3Client.builder().region(region).build();
  private final LambdaClient lambdaClient = LambdaClient.builder().region(region).build();

  @PostMapping(consumes = "application/json")
  public ResponseEntity<Cliente> criar(@RequestBody @Valid final ClienteDto cliente) {
    final Cliente clienteEntidade = new Cliente();
    clienteEntidade.setNome(cliente.getNome());
    clienteEntidade
        .setReferenciaArquivoFoto(cliente.getReferenciaArquivoFoto());
    clienteEntidade
        .setReferenciaArquivoRelatorio(cliente.getReferenciaArquivoRelatorio());

    repository.save(clienteEntidade);

    return ResponseEntity.status(201).body(clienteEntidade);
  }

  @PatchMapping(value = "/foto/{id}", consumes = "image/*")
  public ResponseEntity<Void> atualizarFoto(
      @PathVariable final Long id,
      @RequestBody final byte[] referenciaArquivoFoto) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }

    final String nomeCliente = repository.findByNome(id);
    final String foto = Base64.getEncoder().encodeToString(referenciaArquivoFoto);
    final Lambda lambda = Lambda.FILE_UPLOAD;

    invokeLambdaFunction(lambda, nomeCliente, foto);

    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/foto/{id}", produces = "image/png")
  public ResponseEntity<byte[]> getFoto(@PathVariable final Long id) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }

    final String nome = repository.findByNome(id);
    final Lambda lambda = Lambda.FILE_DOWNLOAD;

    final String base64Image = responseToImage(invokeLambdaFunction(lambda, nome));

    final byte[] imageBytes = Base64.getDecoder().decode(base64Image);

    return ResponseEntity.ok(imageBytes);
  }

  private String invokeLambdaFunction(final Lambda lambda, final String nomeCliente) {
    final String payload = String.format("{ \"nome\": \"%s\" }", nomeCliente);

    final InvokeRequest invokeRequest = InvokeRequest.builder()
        .functionName(lambda.getName())
        .payload(SdkBytes.fromUtf8String(payload))
        .build();

    final InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);

    return invokeResponse.payload().asUtf8String();
  }

  private String invokeLambdaFunction(final Lambda lambda, final String nomeCliente, final String base64EncodedImage) {
    final String payload = String.format("{ \"nome\": \"%s\", \"img\": \"%s\" }", nomeCliente, base64EncodedImage);

    final InvokeRequest invokeRequest = InvokeRequest.builder()
        .functionName(lambda.getName())
        .payload(SdkBytes.fromUtf8String(payload))
        .build();

    final InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);

    final String response = invokeResponse.payload().asUtf8String();
    return response;
  }

  private String responseToImage(final String response) {
    try {
      final JsonNode jsonResponse = new ObjectMapper().readTree(response);
      return jsonResponse.path("image").asText();
    } catch (final Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
