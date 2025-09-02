package com.inside.ddf.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inside.ddf.code.QuestType;
import com.inside.ddf.entity.TB_SR_ITEM;
import com.inside.ddf.entity.TB_SR_QUEST;
import com.inside.ddf.entity.TB_SR_RESP;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.repository.Rep_SR_ITEM;
import com.inside.ddf.repository.Rep_SR_QUEST;
import com.inside.ddf.repository.Rep_SR_RESP;

@Service
public class SurveyService {

	@Autowired
	Rep_SR_QUEST Rep_quest;
	
	@Autowired
	Rep_SR_ITEM Rep_item;
	
	@Autowired
	Rep_SR_RESP Rep_resp;
	
	public List<TB_SR_QUEST> getAll() {
		return Rep_quest.findAll();
	}
	
	public TB_SR_QUEST add(String questCont, QuestType questType) { //객관식 : O , 주관식 : S
		TB_SR_QUEST quest = new TB_SR_QUEST();
		quest.setQuestCont(questCont);
		quest.setQuestType(questType);
		return Rep_quest.save(quest);
	}
	
	public boolean isCompleted(TB_USER user) {
		for (int i=2;i<16;i++) { 
			List<TB_SR_RESP> list = Rep_resp.findAllByUserAndQuest(user, Rep_quest.findById(i).get());
			if (list.size()==0) return false;
		}
		return true;
	}
	
	public void saveResponse(int start,int[] answer, TB_USER user) {
		//start : 2페이지 : 3 / 3페이지 : 6 / 
		for (int i=start;i<start+answer.length;i++) {
			TB_SR_QUEST quest = Rep_quest.findById(i).get();
			TB_SR_ITEM item = Rep_item.findByQuestAndSortOrd(quest, answer[i-start]);
			saveResponseByJPA(user, quest, item);
		}
		
	}
	// 매소드 오버로딩
	public void saveResponse(int start,int[] answer, TB_USER user,Iterable<Integer> dup_answer) {
		//start : 2페이지 : 3 / 3페이지 : 6 / 
		for (int i=start;i<start+answer.length;i++) {
			TB_SR_QUEST quest = Rep_quest.findById(i).get();
			if(i==10 || i==16) {

				List<TB_SR_RESP> prevList= Rep_resp.findAllByUserAndQuest(user, Rep_quest.findById(i).get());
				List<TB_SR_ITEM> itemList = Rep_item.findAllById(dup_answer);
				if (prevList.size()!=0) {
					Rep_resp.deleteAll(prevList);
				}
				for (int j=0;j<itemList.size();j++) {
					TB_SR_RESP resp = new TB_SR_RESP();
					resp.setItem(itemList.get(j));
					resp.setQuest(quest);
					resp.setUser(user);
					Rep_resp.save(resp);
				}
			}
			else {
				TB_SR_ITEM item = Rep_item.findByQuestAndSortOrd(quest, answer[i-start]);
				saveResponseByJPA(user, quest, item);
			}
			
		}
	}
	
	public void saveResponseByJPA(TB_USER user, TB_SR_QUEST quest, TB_SR_ITEM item) {
		Optional<TB_SR_RESP> temp = Rep_resp.findByUserAndQuest(user, quest);

		TB_SR_RESP resp = new TB_SR_RESP();
		if (!temp.isEmpty()) {
			resp = temp.get();
		}
		resp.setItem(item);
		resp.setQuest(quest);
		resp.setUser(user);
		Rep_resp.save(resp);
	}
	
	public List<String> getAllergies(TB_USER user) {
		List<String> result = new ArrayList<>();
		List<TB_SR_RESP> list = Rep_resp.findAllByUserAndQuest(user, Rep_quest.findById(16).get());
		for (int i=0;i<list.size();i++) {
			result.add(list.get(i).getItem().getItemCont());
		}
		return result;
	}
	public List<String> getPreferences(TB_USER user) {
		List<String> result = new ArrayList<>();
		List<TB_SR_RESP> list = Rep_resp.findAllByUserAndQuest(user, Rep_quest.findById(10).get());
		for (int i=0;i<list.size();i++) {
			result.add(list.get(i).getItem().getItemCont());
		}
		return result;
	}
	
	
	
	
	public SurveyCoreInput setClassifyInput(TB_USER user) {
		SurveyCoreInput in = new SurveyCoreInput();
		Double[] three = {85.0, 92.0, 95.0, 105.0, 115.0};
		Double[] six = {120.0, 140.0, 160.0, 180.0, 200.0};
		Double[] twelve = {-0.5, 0.0, 1.0, 1.5, 2.0};
		Boolean[] fourteen1 = {false,true,false,true};
		Boolean[] fourteen2 = {false,false,true,true};
		List<TB_SR_RESP> list= Rep_resp.findAllByUserAndQuest(user, Rep_quest.findById(10).get());
		Set<String> carb = new HashSet<>();
		for(int i=0;i<list.size();i++) carb.add(list.get(i).getItem().getItemCont());
//		for(int i=0;i<list.size();i++) System.out.println(list.get(i).getItem().getItemCont());
//		System.out.println(carb.toString());
//		Rep_resp.findByUserAndQuest(user, Rep_quest.findById(2).get()).getItem().getSortOrd();
//		System.out.println(Rep_resp.findByUserAndQuest(user, Rep_quest.findById(2).get()).getItem().getItemCont());
		in.glucoseMode = Rep_resp.findByUserAndQuest(user, Rep_quest.findById(2).get()).get().getItem().getItemCont(); //2 item.getItem_cont
		in.fpgAvg = three[Rep_resp.findByUserAndQuest(user, Rep_quest.findById(3).get()).get().getItem().getSortOrd()-1]; //3
		in.fpgOverDays_0to7 = Rep_resp.findByUserAndQuest(user, Rep_quest.findById(4).get()).get().getItem().getSortOrd(); //4
		in.lateSnackFreq = Rep_resp.findByUserAndQuest(user, Rep_quest.findById(5).get()).get().getItem().getItemCont(); //5
		in.ppg1hAvg = six[Rep_resp.findByUserAndQuest(user, Rep_quest.findById(6).get()).get().getItem().getSortOrd()-1]; //6 
		in.ppgRise_bf_1to5 = Rep_resp.findByUserAndQuest(user, Rep_quest.findById(7).get()).get().getItem().getSortOrd();
		in.ppgRise_lunch_1to5 = Rep_resp.findByUserAndQuest(user, Rep_quest.findById(8).get()).get().getItem().getSortOrd();
		in.ppgRise_dinner_1to5 = Rep_resp.findByUserAndQuest(user, Rep_quest.findById(9).get()).get().getItem().getSortOrd(); 
		//7 8 9 
		in.carbSources = carb; // 10
		in.coIntakeFiberProtein_1to5 = Rep_resp.findByUserAndQuest(user, Rep_quest.findById(11).get()).get().getItem().getSortOrd(); //11
		in.twoWeeksGainKg = twelve[Rep_resp.findByUserAndQuest(user, Rep_quest.findById(12).get()).get().getItem().getSortOrd()-1]; //12
		in.intakeIncrease_1to5 = Rep_resp.findByUserAndQuest(user, Rep_quest.findById(13).get()).get().getItem().getSortOrd(); //13
		in.insulin = fourteen1[Rep_resp.findByUserAndQuest(user, Rep_quest.findById(14).get()).get().getItem().getSortOrd()-1]; //14
		in.oralMed = fourteen2[Rep_resp.findByUserAndQuest(user, Rep_quest.findById(14).get()).get().getItem().getSortOrd()-1]; // 14
		return in;
	}
	
	
	
	
	@Value("${diet.thresholds.fpg:95}")          private int T_FPG;
    @Value("${diet.thresholds.ppg1h:140}")       private int T_PPG1H;
    @Value("${diet.thresholds.wg.twoweeks-risk-kg:1.5}") private double T_WG_2W_KG;

    public enum CaseLabel {
        F,        // 공복형 위험
        P,        // 식후형 위험
        E,		 // 체중 증가 관리 필요
        I        // 인슐린(또는 경구약) 사용
    }

    public static class SurveyCoreInput {
        public Integer gestWeek;
        public Double weightPre;
        public Double weightNow;
        public String glucoseMode;               // none/smbg/cgm (1,2,3)
        // FPG
        public Double  fpgAvg;
        public Integer fpgSelfFeel_1to5;
        public Integer fpgOverDays_0to7;
        public String  lateSnackFreq;            // none/weekly_1/weekly_2_3/weekly_4_5/daily
        // PPG
        public Double  ppg1hAvg;
        public Integer ppg1hSelfFeel_1to5;
        public Integer ppgRise_bf_1to5;
        public Integer ppgRise_lunch_1to5;
        public Integer ppgRise_dinner_1to5;
        public Set<String> carbSources;          // white_rice_bread,noodles,whole_grain,fruit,dairy,dessert_drink
        public Integer coIntakeFiberProtein_1to5;
        // Weight
        public Double  twoWeeksGainKg;
        public Integer intakeIncrease_1to5;
        // Medication
        public Boolean insulin;
        public Boolean oralMed;
    }

    public static class CaseClassification {
        public final CaseLabel primaryLabel; // 단 한 가지
        public final double fpgScore;        // 참고용
        public final double ppgScore;        // 참고용
        public final double wgScore;         // 참고용
        public final boolean insulinUser;

        public CaseClassification(CaseLabel primaryLabel, double fpgScore, double ppgScore, double wgScore, boolean insulinUser) {
            this.primaryLabel = primaryLabel;
            this.fpgScore = round3(fpgScore);
            this.ppgScore = round3(ppgScore);
            this.wgScore  = round3(wgScore);
            this.insulinUser = insulinUser;
        }
    }

    public CaseClassification classify(SurveyCoreInput in) {
        if (in == null) in = new SurveyCoreInput();
        final String mode = safeLower(in.glucoseMode);

        // 1) 점수 계산 (기존과 동일)
        double fpgBandScore = (isMeasured(mode) && in.fpgAvg != null)
                ? clamp01(scaleByBands_FPG(in.fpgAvg))
                : clamp01(scaleLikert(in.fpgSelfFeel_1to5));
        double fpgOverScore   = clamp01(scaleDays(in.fpgOverDays_0to7));
        double lateSnackScore = clamp01(scaleLateSnack(in.lateSnackFreq));
        double fpgScore = 0.60 * fpgBandScore + 0.25 * fpgOverScore + 0.15 * lateSnackScore;

        double ppgBandScore = (isMeasured(mode) && in.ppg1hAvg != null)
                ? clamp01(scaleByBands_PPG1h(in.ppg1hAvg))
                : clamp01(scaleLikert(in.ppg1hSelfFeel_1to5));
        int bf = nz(in.ppgRise_bf_1to5), lc = nz(in.ppgRise_lunch_1to5), dn = nz(in.ppgRise_dinner_1to5);
        double mealRiseScore   = clamp01(scaleLikert(Math.max(bf, Math.max(lc, dn))));
        double refinedCarbScore= clamp01(scoreRefinedCarb(in.carbSources));
        double coIntakeGood    = clamp01(scaleLikert(in.coIntakeFiberProtein_1to5));
        double coIntakeRisk    = 1.0 - coIntakeGood;
        double ppgScore = 0.50 * ppgBandScore + 0.25 * mealRiseScore + 0.15 * refinedCarbScore + 0.10 * coIntakeRisk;

        double wgGainScore     = clamp01(scaleWeightGain2w(in.twoWeeksGainKg));
        double intakeIncScore  = clamp01(scaleLikert(in.intakeIncrease_1to5));
        double wgScore         = 0.70 * wgGainScore + 0.30 * intakeIncScore;

        // 2) 인슐린/경구약 사용 우선
        boolean insulinUser = Boolean.TRUE.equals(in.insulin) || Boolean.TRUE.equals(in.oralMed);
        if (insulinUser) {
            return new CaseClassification(CaseLabel.I, fpgScore, ppgScore, wgScore, true);
        }

        // 3) 한 가지만 선택 (최대 점수 선택, 동점 시 PPG > FPG > WG)
        CaseLabel primary = pickOnePrimary(fpgScore, ppgScore, wgScore);

        return new CaseClassification(primary, fpgScore, ppgScore, wgScore, false);
    }

    // ---- 단일 선택 로직 ----
    private static CaseLabel pickOnePrimary(double fpgScore, double ppgScore, double wgScore) {
        // 최대값 찾기
        double max = Math.max(fpgScore, Math.max(ppgScore, wgScore));
        final double EPS = 1e-6; // 동점 판정

        boolean tiePPG = Math.abs(ppgScore - max) < EPS;
        boolean tieFPG = Math.abs(fpgScore - max) < EPS;
        boolean tieWG  = Math.abs(wgScore  - max) < EPS;

        // 동점 우선순위: PPG > FPG > WG
        if (tiePPG) return CaseLabel.P;
        if (tieFPG) return CaseLabel.F;
        return CaseLabel.E;
    }

    // ===== 헬퍼 & 스케일러 =====
    private static boolean isMeasured(String mode) {
        return "smbg".equals(mode) || "cgm".equals(mode);
    }
    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }
    private double scaleByBands_FPG(double fpgAvg) {
        if (Double.isNaN(fpgAvg)) return 0.0;
        if (fpgAvg < (T_FPG - 15))   return 0.10;
        if (fpgAvg <  T_FPG)         return 0.40;
        if (fpgAvg <= (T_FPG + 10))  return 0.75;
        return 1.00;
    }
    private double scaleByBands_PPG1h(double ppg1hAvg) {
        if (Double.isNaN(ppg1hAvg)) return 0.0;
        if (ppg1hAvg < (T_PPG1H - 20))  return 0.10;
        if (ppg1hAvg <  T_PPG1H)        return 0.40;
        if (ppg1hAvg <= (T_PPG1H + 20)) return 0.75;
        return 1.00;
    }
    private static double scaleLikert(Integer v) {
        int x = nz(v);
        x = Math.max(1, Math.min(5, x));
        return (x - 1) / 4.0;
    }
    private static double scaleDays(Integer d) {
        int x = Math.max(0, Math.min(7, nz(d)));
        return x / 7.0;
    }
    private static double scaleLateSnack(String freq) {
        String f = safe(freq);
        switch (f) {
            case "none":        return 0.0;
            case "weekly_1":    return 0.25;
            case "weekly_2_3":  return 0.50;
            case "weekly_4_5":  return 0.75;
            case "daily":       return 1.00;
            default:            return 0.50;
        }
    }
    private static String safe(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.ROOT); }

    private static double scoreRefinedCarb(Set<String> sources) {
        if (sources == null || sources.isEmpty()) return 0.0;
        double s = 0.0;
        for (String src : sources) {
            switch (safe(src)) {
                case "white_rice_bread": s += 1.00; break;
                case "noodles":          s += 0.80; break;
                case "dessert_drink":    s += 1.00; break;
                case "fruit":            s += 0.60; break;
                case "dairy":            s += 0.50; break;
                case "whole_grain":      s -= 0.50; break; // 보호요소
                default: break;
            }
        }
        s = s / Math.max(1.0, sources.size());
        return clamp01(s);
    }
    private double scaleWeightGain2w(Double gainKg) {
        if (gainKg == null || Double.isNaN(gainKg)) return 0.5;
        if (gainKg <= 0.0) return 0.0;
        if (gainKg >= T_WG_2W_KG) return 1.0;
        return gainKg / T_WG_2W_KG;
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }
    private static double clamp01(double x) { return x < 0 ? 0 : Math.min(1, x); }
    private static double round3(double x) { return Math.round(x * 1000.0) / 1000.0; }
}
