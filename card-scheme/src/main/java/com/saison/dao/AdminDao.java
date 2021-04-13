package com.saison.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.saison.bean.CardBean;

public class AdminDao {
	JdbcTemplate template;

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	public int saveTest() {

		String sql = "Insert into credit.test(id) values (1) ";
		template.update(sql);
		System.out.println("query done");
		return 0;
	}

	public JSONObject saveCard(String getResponse, String iin) {
		JSONObject obj = new JSONObject(getResponse);

		JSONObject number_obj = (JSONObject) obj.get("number");
		JSONObject country_obj = (JSONObject) obj.get("country");
		JSONObject bank_obj = (JSONObject) obj.get("bank");

		CardBean bean = new CardBean();

		bean.setIin(iin);
		bean.setLength(number_obj.optInt("length", 0));
		bean.setLuhn(number_obj.optBoolean("luhn", false));

		bean.setScheme(obj.optString("scheme", null));
		bean.setType(obj.optString("type", null));
		bean.setBrand(obj.optString("brand", null));
		bean.setPrepaid(obj.optBoolean("prepaid", false));

		bean.setNumeric(country_obj.optString("numeric", null));
		bean.setAlpha2(country_obj.optString("alpha2", null));
		bean.setName(country_obj.optString("name", null));
		bean.setEmoji(country_obj.optString("emoji", null));
		bean.setCurrency(country_obj.optString("currency", null));
		bean.setLatitude(country_obj.optInt("latitude", 0));
		bean.setLongitude(country_obj.optInt("longitude", 0));

		bean.setBank_name(bank_obj.optString("name", null));
		bean.setUrl(bank_obj.optString("url", null));
		bean.setPhone(bank_obj.optString("phone", null));
		bean.setCity(bank_obj.optString("city", null));

		String query_addCard = "insert into credit.card_info"
				+ "(iin, country_numeric, scheme, length, type, brand, bank_name, prepaid, luhn, hits) values" + "("
				+ bean.getIin() + ", " + bean.getNumeric() + ", '" + bean.getScheme() + "', " + bean.getLength() + ", '"
				+ bean.getType() + "', " + "'" + bean.getBrand() + "', '" + bean.getBank_name() + "', "
				+ bean.getPrepaid() + ", " + bean.getLuhn() + ",1)" + "on conflict(iin) "
				+ "do update set hits = credit.card_info.hits+1";

		String query_updateBank = "insert into credit.bank_info(name, url, phone, city) values" + " ('"
				+ bean.getBank_name() + "', '" + bean.getUrl() + "', '" + bean.getPhone() + "', '" + bean.getCity()
				+ "') " + "on conflict(name) do update set url='" + bean.getUrl() + "', phone ='" + bean.getUrl()
				+ "' , city = '" + bean.getCity() + "' ";

		String query_updateCountry = "insert into credit.country_info(alpha2, name, numeric, emoji, currency, latitude, longitude) values "
				+ "('" + bean.getAlpha2() + "', '" + bean.getName() + "', " + bean.getNumeric() + ", '"
				+ bean.getEmoji() + "', '" + bean.getCurrency() + "'," + " " + bean.getLatitude() + ", "
				+ bean.getLongitude() + ") on Conflict(numeric) Do nothing";
		System.out.println(query_addCard);
		System.out.println(query_updateBank);
		System.out.println(query_updateCountry);

		String[] to_update = { query_addCard, query_updateBank, query_updateCountry };
		JSONObject resultObj = new JSONObject();
		try {
			template.batchUpdate(to_update);
			resultObj.put("scheme", bean.getScheme());
			resultObj.put("type", bean.getType());
			resultObj.put("bank", bean.getBank_name());
			resultObj.put("status", 1);

		} catch (Exception e) {
			e.printStackTrace();
			resultObj.put("status", 0);
		}
		return resultObj;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getStats(int start, int limit) {

		String query_info = "Select iin, hits from credit.card_info offset " + start + " limit " + limit + "";
		final JSONObject obj = new JSONObject();
		final JSONObject tempObj = new JSONObject();
		try {
			
			template.query(query_info, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					tempObj.put(rs.getString("iin"), rs.getString("hits"));
					return null;
				}
			});
			obj.put("status", 1);
			obj.put("payload", tempObj);
		} catch (Exception e) {
			e.printStackTrace();
			obj.put("status", 0);
		}
	
		System.out.println(obj);
		return obj;
	}
	
	int a =0;
	public int totalStats() {
		String query_Count = "Select count(iin) from credit.card_info;";
		template.query(query_Count, new RowMapper<Integer>() {
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
			a = rs.getInt("count");
				
				return a;
			}
		});
		return a;
	}
}
