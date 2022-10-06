package com.mdtlabs.coreplatform.spiceservice.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mdtlabs.coreplatform.common.model.entity.spice.NutritionLifestyle;

import java.util.Set;

public interface NutritionLifestyleRepository extends JpaRepository<NutritionLifestyle, Long>  {

    public static final String GET_NUTRITION_LIFESTYLE_BY_IDS = "select nutritionLifestyle from NutritionLifestyle as nutritionLifestyle where nutritionLifestyle.id in (:nutritionLifestyleIds)";

    /**
     * <p>
     * This method used to get the Nutrition Lifestyle using set of ids.
     * </p>
     *
     * @param nutritionLifestyleIds
     * @return Set of NutritionLifestyle Entity
     */
    @Query(value = GET_NUTRITION_LIFESTYLE_BY_IDS)
    public Set<NutritionLifestyle> getNutritionLifestyleByIds(@Param("nutritionLifestyleIds") Set<Long> nutritionLifestyleIds);
}
