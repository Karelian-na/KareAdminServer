/** @format */

const MenuType = {
	Menu: 1,
	Item: 2,
	Page: 3,
	Oper: 4,
};

/**
 * @type FieldsConfig
 */
const specialMenuFieldsConfig = {
	icon: {
		layoutSpan: 16,
		type: "text",
		columnBindProps: {
			className: "icon",
		},
	},
	type: {
		layoutSpan: 8,
		type: "enum",
		enumItems: __COMMON_FIELDS_CONFIGS__.statics.menu.type,
		bindProps: {
			class: "type",
			teleported: false,
		},
	},

	pid: {
		layoutSpan: 24,
		type: "enum",
		bindProps: {
			teleported: false,
		},
	},

	name: {
		layoutSpan: 24,
		type: "text",
		rule: __COMMON_FIELDS_CONFIGS__.requiredRule,
		columnBindProps: {
			className: "name",
			fixed: "left",
			align: "left",
		},
	},

	pmid: {
		layoutSpan: 24,
		type: "enum",
		bindProps: {
			filterable: true,
		},
	},

	oper_id: {
		layoutSpan: 24,
		type: "text",
	},

	oper_type: {
		layoutSpan: 16,
		type: "enum",
		enumItems: __COMMON_FIELDS_CONFIGS__.statics.menu.oper_type,
	},
	status: {
		layoutSpan: 8,
		type: "switch",
		enumItems: [
			{ value: true, label: "启用" },
			{ value: false, label: "禁用" },
		],
		bindProps: {
			"inline-prompt": true,
			"active-text": "启用",
			"inactive-text": "禁用",
		},
	},

	url: {
		layoutSpan: 24,
		type: "text",
		rule: [
			{
				validator(rule, value, callback, source, options) {
					if (!value) {
						value = "";
					}

					const formData = useFormData();
					let regex = [];
					const emptyReg = /^$/g;
					const routeReg = /^\/[\w\/]{2,}\w\/?$/g;
					const urlReg = /^((https?|ftp):\/\/)?([a-zA-Z0-9.-]+)(:[0-9]+)?(\/[^\s]*)?$/;
					let msg;
					switch (formData["type"]) {
						case MenuType.Menu:
							regex.push(emptyReg);
							msg = "菜单类型权限的url必须为空";
							break;
						case MenuType.Item:
							regex.push(emptyReg, routeReg, urlReg);
							msg = "选项类型权限的url必须为空或外链或以 '/' 开头的路由地址";
							break;
						case MenuType.Page:
							regex.push(emptyReg, routeReg, urlReg);
							msg = "标签类型权限的url必须为空或外链或以 '/' 开头的路由地址";
							break;
						case MenuType.Oper:
							regex.push(/^\/[\w\/]{2,}\w$/g, urlReg);
							msg = "操作类型权限的url必须为空外链或以 '/' 开头的路由地址";
							break;
						default:
							callback();
							return;
					}
					if (regex.every((item) => !item.test(value))) {
						callback(new Error(msg));
					} else {
						callback();
					}
				},
				trigger: "blur",
			},
		],
		columnBindProps: {
			align: "left",
		},
	},

	descrip: {
		layoutSpan: 24,
		type: "text",
		rule: {
			max: 100,
			message: "长度不可超过100字!",
			trigger: "blur",
		},
		bindProps: {
			type: "textarea",
			rows: 3,
			resize: "none",
		},
	},
};

/**
 * @type FieldsConfig
 */
export default Object.assign(specialMenuFieldsConfig, __COMMON_FIELDS_CONFIGS__.recordableFieldsConfig);
