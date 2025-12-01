package org.immregitries.clvr;

import com.syadem.nuva.Code;
import com.syadem.nuva.NUVA;
import com.syadem.nuva.Vaccine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.immregitries.clvr.mapping.NUVASystems.*;

public class NUVAService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

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
                vaccineCodeAsId = Integer.parseInt(StringUtils.substringAfter(vaccineCode, "VAC"));
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
