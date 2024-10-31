package sptech.api_arquivos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	@Query("update Cliente c set c.referenciaArquivoFoto = ?2 where c.id = ?1")
	@Modifying
	@Transactional
	void updateReferenciaArquivoFoto(Long id, String referenciaArquivoFoto);

	@Query("update Cliente c set c.referenciaArquivoRelatorio = ?2 where c.id = ?1")
	@Modifying
	@Transactional
	void updateReferenciaArquivoRelatorio(Long id, String referenciaArquivoRelatorio);

	@Query("select c.referenciaArquivoFoto from Cliente c where c.id = ?1")
	String findReferenciaArquivoFotoById(Long id);

	@Query("select c.referenciaArquivoRelatorio from Cliente c where c.id = ?1")
	String findReferenciaArquivoRelatorioById(Long id);

	@Query("select c.nome from Cliente c where c.id = ?1")
    String findByNome(Long id);
}
