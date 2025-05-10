import React, { useState } from "react";
import "./App.css";

function App() {
  const [file, setFile] = useState(null);
  const [ocrText, setOcrText] = useState("");
  const [aiResponse, setAiResponse] = useState("");
  const [invoiceData, setInvoiceData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showOcr, setShowOcr] = useState(false);

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  // JSON yapısını AI çıktısından güvenle ayıkla
  const splitJsonFromAi = (fullText) => {
    try {
      const jsonRegex = /```json\s*([\s\S]*?)\s*```/;
      const match = fullText.match(jsonRegex);
      if (match && match[1]) {
        return JSON.parse(match[1]);
      }
      const fallbackStart = fullText.indexOf("{");
      const fallbackEnd = fullText.lastIndexOf("}");
      const fallbackJson = fullText.slice(fallbackStart, fallbackEnd + 1);
      return JSON.parse(fallbackJson);
    } catch (e) {
      console.error("JSON parse hatası:", e);
      return null;
    }
  };

  // Sadece AI yorumunun "1. Doğal Yorumlama" kısmını al
  const extractNaturalText = (fullText) => {
    const start = fullText.indexOf("**1. Doğal Yorumlama:**");
    const end = fullText.indexOf("**2. Yapılandırılmış JSON Bilgileri:**");
    if (start !== -1 && end !== -1) {
      return fullText.slice(start, end).trim();
    }
    return fullText; // fallback
  };

  const handleSubmit = async () => {
    if (!file) return;
    setLoading(true);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("http://localhost:8080/api/analyze", {
        method: "POST",
        body: formData,
      });

      const data = await response.json();
      setOcrText(data.ocrText);
      setAiResponse(extractNaturalText(data.aiAnalysis));
      setInvoiceData(splitJsonFromAi(data.aiAnalysis));
    } catch (error) {
      setAiResponse("Bir hata oluştu: " + error.message);
    }

    setLoading(false);
  };

  return (
    <div className="App">
      <h2>SmartExpense OCR + AI</h2>

      <div className="upload-section">
        <input type="file" onChange={handleFileChange} />
        <button onClick={handleSubmit} disabled={loading}>
          {loading ? "Yükleniyor..." : "Gönder"}
        </button>
      </div>

      {invoiceData && (
        <div className="card">
          <h3>📋 Fatura Bilgileri</h3>
          <p><strong>Fatura No:</strong> {invoiceData.invoiceNumber}</p>
          <p><strong>Tarih:</strong> {invoiceData.invoiceDate}</p>
          <p><strong>Toplam Tutar:</strong> {invoiceData.totalAmount} {invoiceData.currency}</p>
          <p><strong>Alıcı:</strong> {invoiceData.buyer}</p>
          <p><strong>Satıcı:</strong> {invoiceData.seller || 'Bilgi yok'}</p>
          <p><strong>Ödeme Vadesi:</strong> {invoiceData.dueDate || 'Belirtilmemiş'}</p>
        </div>
      )}

      {aiResponse && (
        <div className="card">
          <h3>🤖 AI Yorumu</h3>
          <div style={{ whiteSpace: "pre-wrap" }}>{aiResponse}</div>
        </div>
      )}

      {ocrText && (
        <div className="card toggle-ocr">
          <button onClick={() => setShowOcr(!showOcr)}>
            {showOcr ? "📄 OCR Çıktısını Gizle" : "📄 OCR Çıktısını Göster"}
          </button>
          {showOcr && <pre>{ocrText}</pre>}
        </div>
      )}
    </div>
  );
}

export default App;
