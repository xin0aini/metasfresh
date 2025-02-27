package de.metas.handlingunits.qrcodes.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import de.metas.global_qrcodes.GlobalQRCode;
import de.metas.global_qrcodes.service.GlobalQRCodeService;
import de.metas.global_qrcodes.service.QRCodePDFResource;
import de.metas.handlingunits.HuId;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.qrcodes.leich_und_mehl.LMQRCode;
import de.metas.handlingunits.qrcodes.model.HUQRCode;
import de.metas.handlingunits.qrcodes.model.HUQRCodeAssignment;
import de.metas.handlingunits.qrcodes.model.IHUQRCode;
import de.metas.process.AdProcessId;
import de.metas.process.PInstanceId;
import de.metas.product.IProductBL;
import de.metas.report.PrintCopies;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.mm.attributes.api.IAttributeDAO;
import org.adempiere.service.ISysConfigBL;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class HUQRCodesService
{
	@NonNull private final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
	@NonNull private final IProductBL productBL = Services.get(IProductBL.class);
	@NonNull private final IAttributeDAO attributeDAO = Services.get(IAttributeDAO.class);
	@NonNull private final ISysConfigBL sysConfigBL = Services.get(ISysConfigBL.class);
	@NonNull private final HUQRCodesRepository huQRCodesRepository;
	@NonNull private final GlobalQRCodeService globalQRCodeService;

	@VisibleForTesting
	static final String SYSCONFIG_GenerateQRCodeIfMissing = "de.metas.handlingunits.qrcodes.GenerateQRCodeIfMissing";

	public HUQRCodesService(
			final @NonNull HUQRCodesRepository huQRCodesRepository,
			final @NonNull GlobalQRCodeService globalQRCodeService)
	{
		this.huQRCodesRepository = huQRCodesRepository;
		this.globalQRCodeService = globalQRCodeService;
	}

	public List<HUQRCode> generate(final HUQRCodeGenerateRequest request)
	{
		return HUQRCodeGenerateCommand.builder()
				.handlingUnitsBL(handlingUnitsBL)
				.productBL(productBL)
				.attributeDAO(attributeDAO)
				.request(request)
				.build()
				.execute();
	}

	public HUQRCodeGenerateForExistingHUsResult generateForExistingHU(final HuId huId)
	{
		return newHUQRCodeGenerateForExistingHUsCommandBuilder()
				.huId(huId)
				.build().execute();
	}

	public HUQRCodeGenerateForExistingHUsResult generateForExistingHUs(@NonNull final Set<HuId> huIds)
	{
		return newHUQRCodeGenerateForExistingHUsCommandBuilder()
				.huIds(huIds)
				.build().execute();
	}

	private HUQRCodeGenerateForExistingHUsCommand.HUQRCodeGenerateForExistingHUsCommandBuilder newHUQRCodeGenerateForExistingHUsCommandBuilder()
	{
		return HUQRCodeGenerateForExistingHUsCommand.builder()
				.handlingUnitsBL(handlingUnitsBL)
				.productBL(productBL)
				.attributeDAO(attributeDAO)
				.huQRCodesRepository(huQRCodesRepository);
	}

	public QRCodePDFResource createPdfForSelectionOfHUIds(@NonNull final PInstanceId selectionId)
	{
		final Set<HuId> huIds = handlingUnitsBL.getHuIdsBySelectionId(selectionId);
		return createPdfForHUIds(huIds);
	}

	public QRCodePDFResource createPdfForSelectionOfHUIds(@NonNull final PInstanceId selectionId, @NonNull final AdProcessId qrCodeProcessId)
	{
		final Set<HuId> huIds = handlingUnitsBL.getHuIdsBySelectionId(selectionId);
		return createPdfForHUIds(huIds, selectionId, qrCodeProcessId);
	}

	public QRCodePDFResource createPdfForHUIds(@NonNull final Set<HuId> huIds)
	{
		// Make sure all HUs have QR Codes assigned
		final ImmutableList<HUQRCode> qrCodes = generateForExistingHUs(huIds).toList();

		return createPDF(qrCodes);
	}

	public QRCodePDFResource createPdfForHUIds(@NonNull final Set<HuId> huIds, @NonNull final PInstanceId selectionId, @NonNull final AdProcessId qrCodeProcessId)
	{
		// Make sure all HUs have QR Codes assigned
		final ImmutableList<HUQRCode> qrCodes = generateForExistingHUs(huIds).toList();
		return createPDF(qrCodes, selectionId, qrCodeProcessId);
	}

	/*
	Creates PDF QR code using the given jasper process
	 */
	public QRCodePDFResource createPDF(@NonNull final List<HUQRCode> qrCodes,
									   @NonNull final PInstanceId pInstanceId,
									   @NonNull final AdProcessId qrCodeProcessId)
	{
		return globalQRCodeService.createPDF(
				qrCodes.stream()
						.map(HUQRCode::toPrintableQRCode)
						.collect(ImmutableList.toImmutableList()),
				pInstanceId,
				qrCodeProcessId
		);
	}

	/*
	Creates PDF QR code using the default jasper process
	 */
	public QRCodePDFResource createPDF(@NonNull final List<HUQRCode> qrCodes)
	{
		return globalQRCodeService.createPDF(
				qrCodes.stream()
						.map(HUQRCode::toPrintableQRCode)
						.collect(ImmutableList.toImmutableList()));
	}

	public void printForSelectionOfHUIds(@NonNull final PInstanceId selectionId, @NonNull final AdProcessId qrCodeProcessId)
	{
		print(createPdfForSelectionOfHUIds(selectionId, qrCodeProcessId));
	}

	public void print(@NonNull final List<HUQRCode> qrCodes)
	{
		print(qrCodes, PrintCopies.ONE);
	}

	public void print(@NonNull final List<HUQRCode> qrCodes, @NonNull PrintCopies copies)
	{
		print(createPDF(qrCodes), copies);
	}

	public void print(@NonNull final QRCodePDFResource pdf)
	{
		print(pdf, PrintCopies.ONE);
	}

	public void print(@NonNull final QRCodePDFResource pdf, @NonNull final PrintCopies copies)
	{
		globalQRCodeService.print(pdf, copies);
	}

	public HuId getHuIdByQRCode(@NonNull final HUQRCode qrCode)
	{
		return getHuIdByQRCodeIfExists(qrCode)
				.orElseThrow(() -> new AdempiereException("No HU attached to QR Code `" + qrCode.toDisplayableQRCode() + "`"));
	}

	public Optional<HuId> getHuIdByQRCodeIfExists(@NonNull final HUQRCode qrCode)
	{
		return getHUAssignmentByQRCode(qrCode)
				.map(HUQRCodeAssignment::getHuId);
	}

	public Optional<HUQRCodeAssignment> getHUAssignmentByQRCode(@NonNull final HUQRCode huQRCode)
	{
		return huQRCodesRepository.getHUAssignmentByQRCode(huQRCode);
	}

	public HUQRCode getQRCodeByHuId(@NonNull final HuId huId)
	{
		final HUQRCode existingQRCode = getFirstQRCodeByHuIdIfExists(huId).orElse(null);
		if (existingQRCode != null)
		{
			return existingQRCode;
		}
		else if (sysConfigBL.getBooleanValue(SYSCONFIG_GenerateQRCodeIfMissing, true))
		{
			return generateForExistingHU(huId).getSingleQRCode(huId);
		}
		else
		{
			throw new AdempiereException("No QR Code attached to HU " + huId.getRepoId());
		}
	}

	public Optional<HUQRCode> getSingleQRCodeByHuIdOrEmpty(@NonNull final HuId huId)
	{
		return huQRCodesRepository.getSingleQRCodeByHuIdOrEmpty(huId);
	}

	public Optional<HUQRCode> getFirstQRCodeByHuIdIfExists(@NonNull final HuId huId)
	{
		return huQRCodesRepository.getFirstQRCodeByHuId(huId);
	}

	public void assign(@NonNull final HUQRCode qrCode, @NonNull final HuId huId)
	{
		huQRCodesRepository.assign(qrCode, huId);
	}

	public void assertQRCodeAssignedToHU(@NonNull final HUQRCode qrCode, @NonNull final HuId huId)
	{
		if (!huQRCodesRepository.isQRCodeAssignedToHU(qrCode, huId))
		{
			throw new AdempiereException("QR Code " + qrCode.toDisplayableQRCode() + " is not assigned to HU " + huId);
		}
	}

	public static IHUQRCode toHUQRCode(@NonNull final String jsonString)
	{
		final GlobalQRCode globalQRCode = GlobalQRCode.ofString(jsonString);
		if (HUQRCode.isHandled(globalQRCode))
		{
			return HUQRCode.fromGlobalQRCode(globalQRCode);
		}
		else if (LMQRCode.isHandled(globalQRCode))
		{
			return LMQRCode.fromGlobalQRCode(globalQRCode);
		}
		else
		{
			throw new AdempiereException("QR code is not handled: " + globalQRCode);
		}
	}
}
