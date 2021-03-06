package GetIt.repositories;

import GetIt.model.repositoriesModels.TranscriptEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranscriptRepository extends CrudRepository<TranscriptEntity, Long> {
}