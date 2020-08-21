package GetIt.repositories;

import GetIt.model.repositoriesModels.DictionaryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DictionariesRepository extends CrudRepository<DictionaryEntity, Long> {
}