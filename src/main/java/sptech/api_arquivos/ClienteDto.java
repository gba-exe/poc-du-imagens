package sptech.api_arquivos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ClienteDto {

	@NotBlank(message = "Nome must not be blank")
	private String nome;

	private String referenciaArquivoFoto;

	private String referenciaArquivoRelatorio;
}
