package com.inside.ddf.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.inside.ddf.entity.TB_RECIPE_LIKEY;
import com.inside.ddf.entity.TB_USER;

public interface Rep_RECIPE_LIKEY extends JpaRepository<TB_RECIPE_LIKEY, Integer>{
	
	boolean existsByRecipe_RcpIdAndUser_UserId(String rcp_id, String userId);

	Integer countByRecipe_RcpId(String rcp_id);

	void deleteByRecipe_RcpIdAndUser_UserId(String rcp_id, String userId);

	@Query("""
		    SELECT l.recipe.rcpId, COUNT(l)
		    FROM TB_RECIPE_LIKEY l
		    GROUP BY l.recipe.rcpId
		    ORDER BY COUNT(l) DESC
		""")
	List<Object[]> findTopLikedRecipes(PageRequest of);
	
	public List<TB_RECIPE_LIKEY> findAllByUser(TB_USER user);
}
