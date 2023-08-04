package com.bytechainx.psi.common.model;

import java.util.List;

import com.bytechainx.psi.common.model.base.BaseGoodsCategory;

/**
 * 商品分类
 */
@SuppressWarnings("serial")
public class GoodsCategory extends BaseGoodsCategory<GoodsCategory> {
	
	public static final GoodsCategory dao = new GoodsCategory().dao();

	
	public GoodsCategory findById(Integer id) {
		return GoodsCategory.dao.findFirst("select * from goods_category where id = ?", id);
	}
	
	/**
	 * 获取直属子类别
	 * @param tenantOrgId
	 * @return
	 */
	public List<GoodsCategory> getChildList() {
		return GoodsCategory.dao.find("select * from goods_category where parent_id = ?", getId());
	}
	/**
	 * 获取父级分类
	 * @return
	 */
	public GoodsCategory getParent() {
		return GoodsCategory.dao.findFirst("select * from goods_category where id = ?", getParentId());
	}
	/**
	 * 获取顶级分类
	 * @param tenantOrgId
	 * @return
	 */
	public List<GoodsCategory> findTop() {
		return GoodsCategory.dao.find("select * from goods_category where parent_id = 0");
	}

	public void loopChilds(List<GoodsCategory> categoryList, StringBuffer levelSb) {
		String name = levelSb.toString();
		if(getParentId().intValue() != 0) {
			name += "└─ " + getName();
		} else {
			name += getName();
		}
		setName(name);
		categoryList.add(this);
		List<GoodsCategory> childList = getChildList();
		if(childList == null || childList.isEmpty()) {
			return;
		}
		if(levelSb.length() == 0) {
			levelSb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
		} else {
			levelSb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		for (GoodsCategory child : childList) {
			child.loopChilds(categoryList, levelSb);
		}
	}
}

