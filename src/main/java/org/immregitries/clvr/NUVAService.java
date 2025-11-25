package org.immregitries.clvr;

import com.syadem.nuva.Code;
import com.syadem.nuva.NUVA;
import com.syadem.nuva.SupportedLocale;
import com.syadem.nuva.Vaccine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NUVAService {
    public static final String NUVA_NOMENCLATURE = "NUVA";
    public static final String NUVA_CODE_PREFIX = "VAC";
    Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NUVA_CVX_NOMENCLATURE = "CVX";
	public static final String NUVA_ATC_NOMENCLATURE = "ATC";
	public static final String NUVA_SCT_NOMENCLATURE = "SNOMED-CT";
	public static final String NUVA_SYSTEM = "urn:oid:1.3.6.1.4.1.48601.1.1.1";

	private final NUVA nuva;

	public NUVAService(NUVA nuva) {
		this.nuva = nuva;
	}

	public Optional<Vaccine> findByCvx(String cvxCode) {
		return findByCode(cvxCode, NUVA_CVX_NOMENCLATURE);
	}


	public Optional<Vaccine> findByCode(String vaccineCode, String nomenclature) {
        Stream<Vaccine> vaccineStream = StreamSupport.stream(nuva.getVaccineRepository().spliterator(), true);
        if (nomenclature.equals(NUVA_NOMENCLATURE)) {
            int vaccineCodeAsId;

            try {
                vaccineCodeAsId = Integer.parseInt(StringUtils.substringAfter(vaccineCode,"VAC"));
            } catch (NumberFormatException numberFormatException) {
                throw new RuntimeException("Code used with NUVA system does not respect format: VACxxxx with x as numbers, thus it can nt be as NUVA id", numberFormatException);
            }
            return Optional.ofNullable(nuva.getVaccineRepository().find(vaccineCodeAsId));
        }
		return vaccineStream.filter(vaccine -> {
			Optional<Code> cvx = vaccine.getCodesList().stream()
				.filter(code -> nomenclature.equals(code.getNomenclature()) && vaccineCode.equals(code.getValue())).findFirst();
			return cvx.isPresent();
		}).findFirst();
	}

	public NUVA getNuva() {
		return nuva;
	}
}
