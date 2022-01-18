package com.br.gabrielsilva.prismamc.commons.core.utils.system;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {
	
	public static String getCalendario() {
		Calendar cal = Calendar.getInstance();
		
		int year = cal.get(Calendar.YEAR), mon = cal.get(Calendar.MONTH)+1, day = cal.get(Calendar.DAY_OF_MONTH),
				hour = cal.get(Calendar.HOUR_OF_DAY), min = cal.get(Calendar.MINUTE), sec = cal.get(Calendar.SECOND);
		
	    StringBuilder newString = new StringBuilder();
	    if (day < 10) {
	    	newString.append("0" + day + "/");
	    } else {
	    	newString.append(day + "/");
	    }
	    
	    if (mon < 10) {
	    	newString.append("0" + mon + "/");
	    } else {
	    	newString.append(mon + "/");
	    }
	    
	    newString.append(year + " - ");
	    
	    if (hour < 10) {
	    	newString.append("0" + hour + ":");
	    } else {
	    	newString.append(hour + ":");
	    }
	    
	    if (min < 10) {
	    	newString.append("0" + min + ":");
	    } else {
	    	newString.append(min + ":");
	    }
	    
	    if (sec < 10) {
	    	newString.append("0" + sec);
	    } else {
	    	newString.append(sec);
	    }
	    return "[" + newString.toString() + "] ";
	}
	
	public static String formatarTempo(Integer i) {
		if (i.intValue() >= 60) {
			Integer time = Integer.valueOf(i.intValue() / 60);
			String add = "";
			if (time.intValue() > 1) {
			  	add = "s";
			}
		    return time + " minuto" + add;
		}
		Integer time = i;
	    String add = "";
		if (time.intValue() > 1) {
		   	add = "s";
		}
		return time + " segundo" + add;
	}
		
	public static String formatarSegundos(Integer i) {
		int minutes = i.intValue() / 60;
	    int seconds = i.intValue() % 60;
	    String disMinu = (minutes < 10 ? "" : "") + minutes;
	    String disSec = (seconds < 10 ? "0" : "") + seconds;
	    String formattedTime = disMinu + ":" + disSec;
	    return formattedTime;
	}
	
	public static String formatarSegundos2(Integer i) {
		int minutes = i.intValue() / 60;
	    int seconds = i.intValue() % 60;
	    
	    String disMinu = (minutes < 10 ? "" : "") + minutes;
	    
	    String disSec = (seconds < 10 ? "0" : "") + seconds;
	    
	    String formattedTime = disMinu + "m " + disSec + "s";
	    
	    return formattedTime;
	}
	
	public static String calcularTempo(FormatUtil formato, int valor) {
		long calculado = System.currentTimeMillis();
		if (formato.equals(FormatUtil.MINUTOS)) {
			calculado = calculado + Long.valueOf(valor * 60000);
		} else if (formato.equals(FormatUtil.HORAS)) {
			calculado = calculado + Long.valueOf(valor * 3600000);
		} else if (formato.equals(FormatUtil.DIAS)) {
			calculado = calculado + Long.valueOf(valor * 86400000);
		} else if (formato.equals(FormatUtil.MESES)) {
			calculado = calculado + (valor * (86400000 * 30));
		}
		return String.valueOf(calculado);
	}
	
	public static String getElapsed(Long started) {
	    Long agora = System.currentTimeMillis();
	    Long time = agora - started;
	    long segundos = time / 1000L % 60L;
	    long minutos = time / (60L * 1000L) % 60L;
	    long horas = time / (60L * 60L * 1000L) % 24L;
	    long dias = time / (24L * 60L * 60L * 1000L);
	    boolean d = false, h = false, m = false, s = false;
	    StringBuilder string = new StringBuilder();
	    if (dias != 0L) {
	    	if (dias == 1) {
	    		string.append(dias + " dia, ");
	    	} else {
	    		string.append(dias + " dias, ");
	    	}
	    	d = true;
	    }
	    if (horas != 0L) {
	    	if (horas == 1) {
	    		string.append(horas + " hora, ");
	    	} else {
	    		string.append(horas + " horas, ");
	    	}
	    	h = true;
	    }
	    if (minutos != 0L) {
	    	if (minutos == 1) {
	    		string.append(minutos + " minuto e ");
	    	} else {
		    	string.append(minutos + " minutos e ");
	    	}
	    	m = true;
	    }
	    if (segundos != 0L) {
	    	if (segundos == 1) {
	    		string.append(segundos + " segundo");
	    	} else {
	    		string.append(segundos + " segundos");
	    	}
	    	s = true;
	    } else {
	    	if ((!d) && (!h) && (!m) && (!s))
	    		return String.valueOf((started - agora)).replaceAll("-", "") + "ms";
	    }
		return string.toString().replaceAll("-", "") + ".";
	}
	
	public enum FormatUtil {
		
		MESES("meses", Arrays.asList("mes")),
		DIAS("dias", Arrays.asList("d", "dia")),
		HORAS("horas", Arrays.asList("h", "hora")),
		MINUTOS("minutos", Arrays.asList("m", "minuto"));
		
		private String nome;
		private List<String> aliases;
		
		FormatUtil(String nome, List<String> aliases) {
			this.nome = nome;
			this.aliases = aliases;
		}

		public String getNome() {
			return nome;
		}
		
		public List<String> getAliases() {
			return this.aliases;
		}

		public static FormatUtil getFromString(String formato) {
			for (FormatUtil formatos : values()) {
				 if (formatos.getNome().toLowerCase().equals(formato.toLowerCase())) {
					 return formatos;
				 }
				 for (String alias : formatos.getAliases()) {
					  if (formato.equalsIgnoreCase(alias)) {
						  return formatos;
					  }
				 }
			}
			return null;
		}
	}
	
	private static String fromLong(long lenth) {
		int days = (int) TimeUnit.SECONDS.toDays(lenth);
		long hours = TimeUnit.SECONDS.toHours(lenth) - days * 24;
		long minutes = TimeUnit.SECONDS.toMinutes(lenth) - TimeUnit.SECONDS.toHours(lenth) * 60L;
		long seconds = TimeUnit.SECONDS.toSeconds(lenth) - TimeUnit.SECONDS.toMinutes(lenth) * 60L;
		String totalDay = days + (days == 1 ? " dia " : " dias ");
		String totalHours = hours + (hours == 1 ? " hora " : " horas ");
		String totalMinutes = minutes + (minutes == 1 ? " minuto " : " minutos ");
		String totalSeconds = seconds + (seconds == 1 ? " segundo" : " segundos");
		
		if (days == 0)
			totalDay = "";
		
		if (hours == 0L)
			totalHours = "";
		
		if (minutes == 0L)
			totalMinutes = "";
		
		if (seconds == 0L)
			totalSeconds = "";
		
		String restingTime = totalDay + totalHours + totalMinutes + totalSeconds;
		restingTime = restingTime.trim();
		
		if (restingTime.equals(""))
			restingTime = "0 segundos";
		
		return restingTime;
	}
	public static String formatDifference(long time) {
		long totalLenth = time;
		long timeLefting = totalLenth - System.currentTimeMillis();
		long seconds = timeLefting / 1000L;
		return fromLong(seconds);
	}
	
	public static long parseDateDiff(String time, boolean future) throws Exception {
		Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?"
				+ "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?"
				+ "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?"
				+ "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
		Matcher m = timePattern.matcher(time);
		int years = 0;
		int months = 0;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		boolean found = false;
		while (m.find()) {
			if (m.group() == null || m.group().isEmpty()) {
				continue;
			}
			for (int i = 0; i < m.groupCount(); i++) {
				if (m.group(i) != null && !m.group(i).isEmpty()) {
					found = true;
					break;
				}
			}
			if (found) {
				if (m.group(1) != null && !m.group(1).isEmpty()) {
					years = Integer.parseInt(m.group(1));
				}
				if (m.group(2) != null && !m.group(2).isEmpty()) {
					months = Integer.parseInt(m.group(2));
				}
				if (m.group(3) != null && !m.group(3).isEmpty()) {
					weeks = Integer.parseInt(m.group(3));
				}
				if (m.group(4) != null && !m.group(4).isEmpty()) {
					days = Integer.parseInt(m.group(4));
				}
				if (m.group(5) != null && !m.group(5).isEmpty()) {
					hours = Integer.parseInt(m.group(5));
				}
				if (m.group(6) != null && !m.group(6).isEmpty()) {
					minutes = Integer.parseInt(m.group(6));
				}
				if (m.group(7) != null && !m.group(7).isEmpty()) {
					seconds = Integer.parseInt(m.group(7));
				}
				break;
			}
		}
		if (!found) {
			throw new Exception("Illegal Date");
		}

		if (years > 20) {
			throw new Exception("Illegal Date");
		}

		Calendar c = new GregorianCalendar();
		if (years > 0) {
			c.add(Calendar.YEAR, years * (future ? 1 : -1));
		}
		if (months > 0) {
			c.add(Calendar.MONTH, months * (future ? 1 : -1));
		}
		if (weeks > 0) {
			c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
		}
		if (days > 0) {
			c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
		}
		if (hours > 0) {
			c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
		}
		if (minutes > 0) {
			c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
		}
		if (seconds > 0) {
			c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
		}
		return c.getTimeInMillis();
	}
}