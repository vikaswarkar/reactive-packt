package se.magnus.microservices.core.product.reactive.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.product.Product;
import se.magnus.microservices.core.product.reactive.persistence.ProductEntity;

@Mapper (componentModel = "spring")
public interface ProductMapper {

	@Mappings({
		@Mapping(target="serviceAddress", ignore = true)
	})
	Product entityToApi(ProductEntity entity);
		
	@Mappings({
		@Mapping(target = "version", ignore = true),
		@Mapping(target = "id", ignore = true)		

	})
	ProductEntity apiToEntity(Product api);
}
