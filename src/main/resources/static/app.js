let globalProvinces = [];
let globalWorkers = [];
let lastAddedWorkerId = null;
let lastAddedProvinceId = null;

const API_TCP_URL = (window.location.protocol === 'file:' || (window.location.port && window.location.port !== '8080'))
    ? 'http://localhost:8080/api/tcp'
    : '/api/tcp';

document.addEventListener('DOMContentLoaded', () => {
    loadAllData();
});

async function sendTcpCommand(payload) {
    try {
        const res = await fetch(API_TCP_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            body: JSON.stringify(payload)
        });
        return await res.json();
    } catch (err) {
        return {
            success: false,
            action: payload.action,
            message: 'Lỗi kết nối: ' + err.message
        };
    }
}

async function loadAllData() {
    const res = await sendTcpCommand({ action: 'GET_ALL_DATA' });
    if (res.success) {
        updateAllViews(res.provinces || [], res.workers || []);
    } else {
        showToast(res.message || 'Không thể tải dữ liệu!', 'error');
    }
}

function updateAllViews(provinces, workers) {
    globalProvinces = provinces;
    globalWorkers = workers;

    renderProvinceDropdowns(provinces);
    renderProvinceTable(provinces);
    renderWorkerTable(workers);
}

function renderProvinceDropdowns(provinces) {
    const addSelect = document.getElementById('queQuanId');
    const searchSelect = document.getElementById('searchQueQuanId');
    const editSelect = document.getElementById('editQueQuanId');

    const prevAddVal = addSelect.value;
    const prevSearchVal = searchSelect.value;
    const prevEditVal = editSelect.value;

    addSelect.innerHTML = '<option value="">-- Chọn quê quán từ danh sách --</option>';
    searchSelect.innerHTML = '<option value="">-- Tất cả Quê quán --</option>';
    editSelect.innerHTML = '';

    provinces.forEach(p => {
        const label = `${p.tenTinh}${p.moTa ? ' — ' + p.moTa : ''}`;

        const optAdd = document.createElement('option');
        optAdd.value = p.id;
        optAdd.textContent = label;
        addSelect.appendChild(optAdd);

        const optSearch = document.createElement('option');
        optSearch.value = p.id;
        optSearch.textContent = p.tenTinh;
        searchSelect.appendChild(optSearch);

        const optEdit = document.createElement('option');
        optEdit.value = p.id;
        optEdit.textContent = label;
        editSelect.appendChild(optEdit);
    });

    if (prevAddVal) addSelect.value = prevAddVal;
    if (prevSearchVal) searchSelect.value = prevSearchVal;
    if (prevEditVal) editSelect.value = prevEditVal;
}

function renderProvinceTable(provinces) {
    const tbody = document.getElementById('provinceTableBody');

    if (!provinces.length) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center">Chưa có Tỉnh/TP nào.</td></tr>';
        return;
    }

    tbody.innerHTML = provinces.map(p => {
        const highlight = p.id === lastAddedProvinceId ? 'class="highlight-new"' : '';
        return `
            <tr ${highlight}>
                <td>#${p.id}</td>
                <td><span class="province-pill">${escapeHtml(p.tenTinh)}</span></td>
                <td>${escapeHtml(p.moTa || '')}</td>
            </tr>
        `;
    }).join('');
}

function renderWorkerTable(workers) {
    const tbody = document.getElementById('workerTableBody');

    if (!workers.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Không có công nhân nào.</td></tr>';
        return;
    }

    tbody.innerHTML = workers.map(w => {
        const highlight = w.id === lastAddedWorkerId ? 'class="highlight-new"' : '';
        return `
            <tr ${highlight}>
                <td>#${w.id}</td>
                <td><strong>${escapeHtml(w.hoTen)}</strong></td>
                <td>${w.namSinh}</td>
                <td><span class="province-pill">${escapeHtml(w.tenQueQuan || '')}</span></td>
                <td class="salary-tag">${formatCurrency(w.mucLuong)}</td>
                <td class="text-center">
                    <button type="button" class="btn btn-edit" onclick="openEditModal(${w.id})">Cập nhật</button>
                    <button type="button" class="btn btn-del" onclick="handleDeleteWorkerById(${w.id}, '${escapeJs(w.hoTen)}')">Xóa</button>
                </td>
            </tr>
        `;
    }).join('');
}

// 1. Thêm Tỉnh/TP
async function handleAddProvince(event) {
    event.preventDefault();
    const tenTinh = document.getElementById('tenTinh').value.trim();
    const moTa = document.getElementById('moTaTinh').value.trim();

    const res = await sendTcpCommand({
        action: 'ADD_PROVINCE',
        province: { tenTinh, moTa }
    });

    if (res.success && res.addedProvince) {
        lastAddedProvinceId = res.addedProvince.id;
        updateAllViews(res.provinces || [], res.workers || []);
        document.getElementById('queQuanId').value = res.addedProvince.id;
        document.getElementById('formAddProvince').reset();

        renderAddResult('PROVINCE', true, res.message, res.addedProvince);
        showToast(res.message, 'success');
    } else {
        renderAddResult('PROVINCE', false, res.message || 'Thêm Tỉnh/TP thất bại!', { tenTinh, moTa });
        showToast(res.message || 'Thêm Tỉnh/TP thất bại!', 'error');
    }
}

// 2. Thêm Công nhân (chọn quê quán từ danh sách)
async function handleAddWorker(event) {
    event.preventDefault();
    const hoTen = document.getElementById('hoTen').value.trim();
    const namSinh = parseInt(document.getElementById('namSinh').value, 10);
    const queQuanId = parseInt(document.getElementById('queQuanId').value, 10);
    const mucLuong = parseFloat(document.getElementById('mucLuong').value);

    const res = await sendTcpCommand({
        action: 'ADD_WORKER',
        worker: { hoTen, namSinh, queQuanId, mucLuong }
    });

    if (res.success && res.addedWorker) {
        lastAddedWorkerId = res.addedWorker.id;
        updateAllViews(res.provinces || [], res.workers || []);
        document.getElementById('hoTen').value = '';

        renderAddResult('WORKER', true, res.message, res.addedWorker);
        showToast(res.message, 'success');
    } else {
        renderAddResult('WORKER', false, res.message || 'Thêm Công nhân thất bại!', {
            hoTen, namSinh, queQuanId, mucLuong
        });
        showToast(res.message || 'Thêm Công nhân thất bại!', 'error');
    }
}

// 3. Hiển thị kết quả Thêm
function renderAddResult(type, success, message, entity) {
    const box = document.getElementById('addResultContainer');
    box.className = 'add-result-box' + (success ? '' : ' error');

    if (type === 'PROVINCE') {
        box.innerHTML = `
            <span class="result-badge ${success ? '' : 'err'}">
                ${success ? 'Thêm Tỉnh/TP thành công' : 'Thêm Tỉnh/TP thất bại'}
            </span>
            <div>${escapeHtml(message)}</div>
            <div class="result-details">
                <div><strong>Tên Tỉnh/TP:</strong> ${escapeHtml(entity.tenTinh || '')}</div>
                <div><strong>Mô tả:</strong> ${escapeHtml(entity.moTa || '')}</div>
            </div>
        `;
    } else {
        box.innerHTML = `
            <span class="result-badge ${success ? '' : 'err'}">
                ${success ? 'Thêm Công nhân thành công' : 'Thêm Công nhân thất bại'}
            </span>
            <div>${escapeHtml(message)}</div>
            <div class="result-details">
                <div><strong>Họ tên:</strong> ${escapeHtml(entity.hoTen || '')}</div>
                <div><strong>Năm sinh:</strong> ${entity.namSinh || ''}</div>
                <div><strong>Quê quán:</strong> ${escapeHtml(entity.tenQueQuan || '')}</div>
                <div><strong>Mức lương:</strong> ${entity.mucLuong ? formatCurrency(entity.mucLuong) : ''}</div>
            </div>
        `;
    }
}

// 4. Tìm kiếm và hiển thị theo quê quán / khoảng năm sinh / khoảng lương
async function handleSearchWorkers(event) {
    event.preventDefault();
    const queQuanVal = document.getElementById('searchQueQuanId').value;
    const minNamVal = document.getElementById('searchMinNamSinh').value;
    const maxNamVal = document.getElementById('searchMaxNamSinh').value;
    const minLuongVal = document.getElementById('searchMinLuong').value;
    const maxLuongVal = document.getElementById('searchMaxLuong').value;

    const res = await sendTcpCommand({
        action: 'SEARCH_WORKERS',
        queQuanId: queQuanVal ? parseInt(queQuanVal, 10) : null,
        minNamSinh: minNamVal ? parseInt(minNamVal, 10) : null,
        maxNamSinh: maxNamVal ? parseInt(maxNamVal, 10) : null,
        minMucLuong: minLuongVal ? parseFloat(minLuongVal) : null,
        maxMucLuong: maxLuongVal ? parseFloat(maxLuongVal) : null
    });

    if (res.success) {
        renderWorkerTable(res.workers || []);
        showToast(res.message, 'success');
    } else {
        showToast(res.message || 'Lỗi khi tìm kiếm!', 'error');
    }
}

function resetSearch() {
    document.getElementById('formSearchWorker').reset();
    loadAllData();
}

// 5. Cập nhật thông tin công nhân
function openEditModal(workerId) {
    // Mở form và điền dữ liệu công nhân đang được chọn.
    const worker = globalWorkers.find(w => w.id === workerId);
    if (!worker) return;

    document.getElementById('editWorkerId').value = worker.id;
    document.getElementById('editHoTen').value = worker.hoTen;
    document.getElementById('editNamSinh').value = worker.namSinh;
    document.getElementById('editMucLuong').value = worker.mucLuong;
    document.getElementById('editQueQuanId').value = worker.queQuanId;

    document.getElementById('editModal').classList.remove('hidden');
}

function closeEditModal() {
    document.getElementById('editModal').classList.add('hidden');
}

async function handleUpdateWorker(event) {
    // Gửi thông tin đã chỉnh sửa qua TCP Server để cập nhật vào MySQL.
    event.preventDefault();
    const id = parseInt(document.getElementById('editWorkerId').value, 10);
    const hoTen = document.getElementById('editHoTen').value.trim();
    const namSinh = parseInt(document.getElementById('editNamSinh').value, 10);
    const queQuanId = parseInt(document.getElementById('editQueQuanId').value, 10);
    const mucLuong = parseFloat(document.getElementById('editMucLuong').value);

    const res = await sendTcpCommand({
        action: 'UPDATE_WORKER',
        worker: { id, hoTen, namSinh, queQuanId, mucLuong }
    });

    if (res.success) {
        lastAddedWorkerId = id;
        closeEditModal();
        updateAllViews(res.provinces || [], res.workers || []);
        showToast(res.message, 'success');
    } else {
        showToast(res.message || 'Cập nhật thất bại!', 'error');
    }
}

// 6. Xóa một công nhân
async function handleDeleteWorkerById(workerId, hoTen) {
    // Xác nhận rồi gửi yêu cầu xóa công nhân theo ID.
    if (!confirm(`Xóa công nhân "${hoTen}"?`)) {
        return;
    }
    const res = await sendTcpCommand({
        action: 'DELETE_WORKER_BY_ID',
        workerId
    });
    if (res.success) {
        updateAllViews(res.provinces || [], res.workers || []);
        showToast(res.message, 'success');
    } else {
        showToast(res.message || 'Xóa thất bại!', 'error');
    }
}

// 7. Xóa công nhân theo từ khóa
async function handleDeleteByKeyword(event) {
    // Xóa các công nhân có tên hoặc quê quán khớp từ khóa.
    event.preventDefault();
    const keyword = document.getElementById('deleteKeyword').value.trim();
    if (!keyword) return;

    if (!confirm(`Xóa các công nhân theo từ khóa "${keyword}"?`)) {
        return;
    }

    const res = await sendTcpCommand({
        action: 'DELETE_WORKERS_BY_KEYWORD',
        keyword
    });

    if (res.success) {
        document.getElementById('formDeleteByKeyword').reset();
        updateAllViews(res.provinces || [], res.workers || []);
        showToast(res.message, 'success');
    } else {
        showToast(res.message || 'Xóa thất bại!', 'error');
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function escapeJs(str) {
    if (!str) return '';
    return String(str).replace(/'/g, "\\'");
}
