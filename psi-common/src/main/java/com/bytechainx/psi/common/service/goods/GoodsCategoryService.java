package com.bytechainx.psi.common.service.goods;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.GoodsCategory;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 商品分类
*/
public class GoodsCategoryService extends CommonService {
	
	/**
	* 分页列表
	*/
	public Page<GoodsCategory> paginate(Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");


		return GoodsCategory.dao.paginate(pageNumber, pageSize, "select * ", "from goods_category "+where.toString()+" order by id desc", params.toArray());
	}


	/**
	* 新增
	*/
	public Ret create(GoodsCategory category) {
		if(StringUtils.isEmpty(category.getName())) {
			return Ret.fail("分类名称不能为空");
		}
		GoodsCategory _category = GoodsCategory.dao.findFirst("select * from goods_category where name = ? limit 1", category.getName());
		if(_category != null) {
			return Ret.fail("分类名称已存在");
		}
		category.setCreatedAt(new Date());
		category.save();
		
		return Ret.ok("新增商品分类成功").set("targetId", category.getId());
	}


	/**
	* 修改
	*/
	public Ret update(GoodsCategory category) {
		if(StringUtils.isEmpty(category.getName())) {
			return Ret.fail("分类名称不能为空");
		}
		GoodsCategory _category = GoodsCategory.dao.findFirst("select * from goods_category where name = ? limit 1", category.getName());
		if(_category != null && _category.getId().intValue() != category.getId().intValue()) {
			return Ret.fail("分类名称已存在");
		}
		_category = GoodsCategory.dao.findById(category.getId());
		if(_category == null) {
			return Ret.fail("商品分类不存在，无法修改");
		}
		
		category.update();
		
		return Ret.ok("修改商品分类成功");
	}


	/**
	* 删除
	*/
	public Ret delete(Integer id) {
		GoodsCategory goodsCategory = GoodsCategory.dao.findById(id);
		List<GoodsCategory> childList = goodsCategory.getChildList();
		if(!childList.isEmpty()) {
			return Ret.fail("存在子分类，无法删除");
		}
		goodsCategory.delete();
		Db.update("update goods_info set goods_category_id = ? where goods_category_id = ?", 0, id);
		
		return Ret.ok("删除商品分类成功");
	}

}