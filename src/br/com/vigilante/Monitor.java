package br.com.vigilante;

import java.sql.Date;

import br.com.dao.MonitorDao;

public class Monitor {
	private Date moment_capture;
	private Boolean is_true;
	private Integer camera_id;
	public Date getMoment_capture() {
		return moment_capture;
	}
	public void setMoment_capture(Date moment_capture) {
		this.moment_capture = moment_capture;
	}
	public Boolean getIs_true() {
		return is_true;
	}
	public void setIs_true(Boolean is_true) {
		this.is_true = is_true;
	}
	public Integer getCamera_id() {
		return camera_id;
	}
	public void setCamera_id(Integer camera_id) {
		this.camera_id = camera_id;
	}
	public static void incluir() {
		MonitorDao mDao = new MonitorDao();
		mDao.CriarRegistro();
	}
}
